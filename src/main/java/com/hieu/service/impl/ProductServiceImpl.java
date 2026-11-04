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
import com.hieu.service.PricingService;
import com.hieu.service.ProductService;
import com.hieu.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final UploadService uploadService;
    private final PricingService pricingService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> findPaged(Pageable pageable) {
        return toPage(productRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> findByCategory(Long categoryId, Pageable pageable) {
        return toPage(productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> search(String keyword, Long categoryId, Pageable pageable) {
        Page<Product> result = (categoryId == null)
                ? productRepository.search(keyword == null ? "" : keyword, pageable)
                : productRepository.searchByCategory(categoryId, keyword == null ? "" : keyword, pageable);
        return toPage(result);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsLatest", key = "#p0.pageNumber + '-' + #p0.pageSize")
    public PageResponse<ProductDto> findLatest(Pageable pageable) {
        return toPage(productRepository.findLatest(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return toDto(getProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("productsRescue")
    public List<ProductDto> findRescue() {
        List<Product> products = productRepository.findWithSellableBatches(
                com.hieu.enums.BatchStatus.ACTIVE, PricingService.today());
        Map<Long, LocalDate> expiry = pricingService.earliestExpiry(products.stream().map(Product::getId).toList());
        return products.stream()
                .map(p -> enrich(ProductMapper.toDto(p), p, expiry.get(p.getId())))
                .filter(d -> d.getDiscountPercent() != null && d.getDiscountPercent() > 0)
                .sorted(Comparator.comparing(ProductDto::getDiscountPercent).reversed())
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsLatest", "productsRescue"}, allEntries = true)
    public ProductDto create(ProductDto dto) {
        Category category = resolveCategory(dto.getCategoryId());
        Product product = Product.builder()
                .name(dto.getName())
                .image(dto.getImage())
                .price(dto.getPrice())
                .active(dto.getActive() == null ? true : dto.getActive())
                .description(dto.getDescription())
                .shelfLifeDays(dto.getShelfLifeDays())
                .category(category)
                .build();
        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsLatest", "productsRescue"}, allEntries = true)
    public ProductDto update(Long id, ProductDto dto) {
        Product product = getProduct(id);
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getImage() != null) product.setImage(dto.getImage());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getActive() != null) product.setActive(dto.getActive());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getShelfLifeDays() != null) product.setShelfLifeDays(dto.getShelfLifeDays());
        if (dto.getCategoryId() != null) product.setCategory(resolveCategory(dto.getCategoryId()));
        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsLatest", "productsRescue"}, allEntries = true)
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
    @CacheEvict(value = {"productsLatest", "productsRescue"}, allEntries = true)
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsLatest", "productsRescue"}, allEntries = true)
    public ProductImageDto addImage(Long productId, MultipartFile file) {
        Product product = getProduct(productId);
        String path = uploadService.save(file, "Product");
        ProductImage image = ProductImage.builder().imageUrl(path).product(product).build();
        return ProductMapper.toImageDto(productImageRepository.save(image));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productsLatest", "productsRescue"}, allEntries = true)
    public void deleteImage(Long imageId) {
        if (!productImageRepository.existsById(imageId)) {
            throw new RuntimeException("Product image not found: " + imageId);
        }
        productImageRepository.deleteById(imageId);
    }

    private PageResponse<ProductDto> toPage(Page<Product> page) {
        Map<Long, LocalDate> expiry = pricingService.earliestExpiry(
                page.getContent().stream().map(Product::getId).toList());
        return PageResponse.of(page, p -> enrich(ProductMapper.toDto(p), p, expiry.get(p.getId())));
    }

    private ProductDto toDto(Product product) {
        LocalDate expiry = pricingService.earliestExpiry(List.of(product.getId())).get(product.getId());
        return enrich(ProductMapper.toDto(product), product, expiry);
    }

    private ProductDto enrich(ProductDto dto, Product product, LocalDate expiry) {
        int percent = pricingService.discountPercent(product.getShelfLifeDays(), expiry);
        dto.setDiscountPercent(percent);
        dto.setCurrentPrice(pricingService.applyDiscount(product.getPrice(), percent));
        dto.setNearestExpiry(expiry);
        dto.setDaysLeft(expiry == null ? null : (int) ChronoUnit.DAYS.between(PricingService.today(), expiry));
        return dto;
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
