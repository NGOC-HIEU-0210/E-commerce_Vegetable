package com.hieu.service.impl;

import com.hieu.mapper.ProductMapper;
import com.hieu.model.Category;
import com.hieu.model.Product;
import com.hieu.model.ProductImage;
import com.hieu.payload.dto.ProductDto;
import com.hieu.payload.dto.ProductImageDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.CategoryRepository;
import com.hieu.repository.ProductImageRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.service.ProductService;
import com.hieu.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final UploadService uploadService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll().stream().map(ProductMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> findPaged(Pageable pageable) {
        return PageResponse.of(productRepository.findAll(pageable), ProductMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return ProductMapper.toDto(getProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(ProductMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> search(String keyword, Long categoryId) {
        List<Product> result = (categoryId == null)
                ? productRepository.search(keyword == null ? "" : keyword)
                : productRepository.searchByCategory(categoryId, keyword == null ? "" : keyword);
        return result.stream().map(ProductMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findLatest(int limit) {
        return productRepository.findLatest(PageRequest.of(0, limit)).stream()
                .map(ProductMapper::toDto).toList();
    }

    @Override
    @Transactional
    public ProductDto create(ProductDto dto) {
        Category category = resolveCategory(dto.getCategoryId());
        Product product = Product.builder()
                .name(dto.getName())
                .image(dto.getImage())
                .price(dto.getPrice())
                .active(dto.getActive() == null ? true : dto.getActive())
                .description(dto.getDescription())
                .category(category)
                .build();
        return ProductMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product product = getProduct(id);
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getImage() != null) product.setImage(dto.getImage());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getActive() != null) product.setActive(dto.getActive());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) product.setCategory(resolveCategory(dto.getCategoryId()));
        return ProductMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDto setImage(Long id, MultipartFile image) {
        Product product = getProduct(id);
        String path = uploadService.save(image, "Product");
        if (path != null) {
            product.setImage(path);
        }
        return ProductMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProductImageDto addImage(Long productId, MultipartFile file) {
        Product product = getProduct(productId);
        String path = uploadService.save(file, "Product");
        ProductImage image = ProductImage.builder().imageUrl(path).product(product).build();
        return ProductMapper.toImageDto(productImageRepository.save(image));
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        if (!productImageRepository.existsById(imageId)) {
            throw new RuntimeException("Product image not found: " + imageId);
        }
        productImageRepository.deleteById(imageId);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
    }
}
