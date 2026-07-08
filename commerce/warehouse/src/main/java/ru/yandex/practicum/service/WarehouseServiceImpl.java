package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.exception.warehouse.*;
import ru.yandex.practicum.mapper.AddressMapper;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {
    private final AddressRepository addressRepository;
    private final WarehouseRepository warehouseRepository;
    private final AddressMapper addressMapper;
    private final WarehouseProductMapper warehouseProductMapper;
    private final UUID idAddress;
    private final OrderBookingRepository orderBookingRepository;

    public WarehouseServiceImpl(AddressRepository addressRepository, WarehouseRepository warehouseRepository,
                                AddressMapper addressMapper, WarehouseProductMapper warehouseProductMapper,
                                OrderBookingRepository orderBookingRepository) {
        this.addressRepository = addressRepository;
        this.warehouseRepository = warehouseRepository;
        this.addressMapper = addressMapper;
        this.warehouseProductMapper = warehouseProductMapper;
        String[] address = {"ADDRESS_1", "ADDRESS_2"};
        int randomIdx = Random.from(new SecureRandom()).nextInt(0, address.length);
        this.idAddress = addressRepository.save(Address.createAddress(address[randomIdx])).getId();
        this.orderBookingRepository = orderBookingRepository;
    }

    @Override
    @Transactional
    public void newProduct(WarehouseRequest newRequest) {
        if (warehouseRepository.existsById(newRequest.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар с ID = "
                    + newRequest.getProductId() + "уже зарегистрирован.");
        }

        WarehouseProduct product = warehouseProductMapper.mapToWarProduct(newRequest);
        warehouseRepository.save(product);
    }

    private BookedDto checkQuantityProducts(Map<UUID, Integer> wantedProducts,
                                            Map<UUID, WarehouseProduct> productMap) {
        BookedDto result = BookedDto.builder()
                .deliveryVolume(0.0)
                .deliveryWeight(0.0)
                .fragile(false)
                .build();

        for (Map.Entry<UUID, Integer> entry : wantedProducts.entrySet()) {
            UUID id = entry.getKey();
            Integer wanted = entry.getValue();
            WarehouseProduct product = productMap.get(id);
            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException("Товар с ID " + id + " не найден");
            }
            if (wanted > product.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Недостаточно товара " + id + ", доступно " + product.getQuantity()
                );
            }
            Dimension dim = product.getDimension();
            if (dim != null) {
                double volume = dim.getHeight() * dim.getWidth() * dim.getDepth();
                result.setDeliveryVolume(result.getDeliveryVolume() + volume);
            }
            result.setDeliveryWeight(result.getDeliveryWeight() + product.getWeight() * wanted);
            if (product.getFragile() != null) {
                result.setFragile(result.getFragile() || product.getFragile());
            }
        }
        return result;
    }

    @Override
    public BookedDto checkQuantityProducts(CartDto cartDto) {
        Map<UUID, Integer> products = cartDto.getProducts();
        Map<UUID, WarehouseProduct> productMap = warehouseRepository.findAllAsMapByIds(products.keySet());
        return checkQuantityProducts(products, productMap);

//        Set<UUID> ids = shoppingCartDto.getProducts().keySet();
//        Map<UUID, WarehouseProduct> productById = warehouseRepository.findAllAsMapByIds(ids);
//
//        BookedDto result = BookedDto.builder()
//                .deliveryVolume(0.0)
//                .deliveryWeight(0.0)
//                .fragile(false)
//                .build();
//
//        List<ProductNotEnough> productsNotEnough = new ArrayList<>();
//        List<UUID> productsNotFound = new ArrayList<>();
//
//        for (Map.Entry<UUID, Integer> entry : shoppingCartDto.getProducts().entrySet()) {
//            UUID id = entry.getKey();
//            Integer wantedCount = entry.getValue();
//
//            if (!productById.containsKey(id)) {
//                productsNotFound.add(id);
//                continue;
//            }
//
//            WarehouseProduct product = productById.get(id);
//
//            Integer availableCount = product.getQuantity();
//
//            if (wantedCount > availableCount) {
//                productsNotEnough.add(new ProductNotEnough(id, availableCount, wantedCount));
//                continue;
//            }
//
//            Dimension dimension = product.getDimension();
//
//            Double currentVolume = result.getDeliveryVolume();
//            Double addVolume = dimension.getHeight() * dimension.getWidth() * dimension.getDepth();
//            Double newVolume = currentVolume + addVolume;
//            result.setDeliveryVolume(newVolume);
//
//            Double currentWeight = result.getDeliveryWeight();
//            Double addWeight = product.getWeight() * wantedCount;
//            Double newWeight = currentWeight + addWeight;
//            result.setDeliveryWeight(newWeight);
//
//            if (product.getFragile() != null) {
//                boolean fragile = result.getFragile() || product.getFragile();
//                result.setFragile(fragile);
//            }
//        }
//
//        if (!productsNotFound.isEmpty()) {
//            throw new NoSpecifiedProductInWarehouseException("Нет информации о товарах на складе ID: " + productsNotFound);
//        }
//
//        if (!productsNotEnough.isEmpty()) {
//            throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товаров на складе: " + productsNotEnough);
//        }
//
//        return result;
    }

    @Override
    @Transactional
    public void addQuantityProduct(AddToCartRequest addRequest) {
        WarehouseProduct product = warehouseRepository.findById(addRequest.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException
                        (String.format("Товара с ID = %s нeт на складе", addRequest.getProductId())));
        product.setQuantity(product.getQuantity() + addRequest.getQuantity());
    }

    @Override
    public AddressDto getAddress() {
        Address address = addressRepository.findById(idAddress)
                .orElseThrow(() -> new IllegalStateException("Адрес в БД не найден, ID = " + idAddress));
        return addressMapper.mapToAddressDto(address);
    }

    @Override
    @Transactional
    public void shippedProductForDelivery(DeliveryRequest shippedRequest) {
        OrderBooking orderBooking = orderBookingRepository.findById(shippedRequest.getOrderId())
                .orElseThrow(() -> new OrderBookingNotFoundException(
                        String.format("Для заказа с ID %s бронирование не найдено",shippedRequest.getOrderId())));

        orderBooking.setDeliveryId(shippedRequest.getDeliveryId());
    }

    @Override
    @Transactional
    public void returnProductToTheWarehouse(Map<UUID, Integer> products) {
        Map<UUID, WarehouseProduct> warehouseProducts = warehouseRepository.findAllById(products.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        List<UUID> missingIds = new ArrayList<>();

        products.forEach((productId, quantity) -> {
            if (!warehouseProducts.containsKey(productId)) {
                missingIds.add(productId);
            } else {
                WarehouseProduct warehouseProduct = warehouseProducts.get(productId);
                warehouseProduct.setQuantity(warehouseProduct.getQuantity() + quantity);
            }
        });

        if (!missingIds.isEmpty()) {
            String errorMessage = "На складе отсутствуют товары: " +
                    missingIds.stream()
                            .map(UUID::toString)
                            .collect(Collectors.joining(", "));

            throw new MultipleProductsNotFoundException(errorMessage, missingIds);
        }
    }

    @Override
    @Transactional
    public BookedDto assemblyProductOnOrderForDelivery(ProductsOrderRequest assemblyRequest) {
        Map<UUID, Integer> assemblyProducts = assemblyRequest.getProducts();

        Map<UUID, WarehouseProduct> warehouseProducts = warehouseRepository
                .findAllById(assemblyProducts.keySet()).stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        BookedDto bookedProducts = checkQuantityProducts(assemblyProducts, warehouseProducts);

        warehouseProducts.forEach((key, value) -> value.setQuantity(value.getQuantity() - assemblyProducts.get(key)));

        orderBookingRepository.save(OrderBooking.builder()
                .products(assemblyProducts)
                .orderId(assemblyRequest.getOrderId())
                .build());

        return bookedProducts;
    }
}