package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.ProductPageDto;
import ru.yandex.practicum.dto.store.StateRequest;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exception.store.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    private Product getProduct(UUID productId) {
        return productRepository.findById(productId).
                orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + productId + " NOT FOUND"));
    }

    @Override
    public ProductPageDto getAllProducts(ProductCategory category, Pageable pageable) {
        List<Product> products = productRepository.findByProductCategory(category, pageable);

        List<ProductDto> productsDto = products.stream()
                .map(mapper::mapToProductDto)
                .toList();

        return new ProductPageDto(productsDto, pageable.getSort());
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = mapper.mapToProduct(productDto);
        return mapper.mapToProductDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        Product product = getProduct(productDto.getProductId());
        mapper.updateProductFromDto(product, productDto);

        return mapper.mapToProductDto(product);
    }

    @Override
    @Transactional
    public Boolean removeProductById(UUID productId) {
        Product product = getProduct(productId);

        if (product.getProductState().equals(ProductState.DEACTIVATE)) {
            return false;
        }
        product.setProductState(ProductState.DEACTIVATE);
        return true;
    }

    @Override
    @Transactional
    public Boolean setProductQuantityState(StateRequest stateRequest) {
        Product product = getProduct(stateRequest.getProductId());
        product.setQuantityState(stateRequest.getQuantityState());
        return true;
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        Product product = getProduct(productId);
        return mapper.mapToProductDto(product);
    }
}