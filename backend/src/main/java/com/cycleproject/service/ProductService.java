package com.cycleproject.service;

import com.cycleproject.dto.ApiResponse;
import com.cycleproject.dto.ProductRequest;
import com.cycleproject.entity.*;
import com.cycleproject.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductPriceRepository priceRepository;
    private final CustomerGroupRepository groupRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository, ProductPriceRepository priceRepository,
                          CustomerGroupRepository groupRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.groupRepository = groupRepository;
    }

    public ApiResponse addProduct(ProductRequest request, MultipartFile[] images, MultipartFile[] videos) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .sku(request.getSku())
                .stockQuantity(request.getStockQuantity())
                .active(true)
                .build();

        product = productRepository.save(product);

        // Save prices for each group
        if (request.getPrices() != null) {
            for (ProductRequest.GroupPrice gp : request.getPrices()) {
                CustomerGroup group = groupRepository.findById(gp.getGroupId()).orElse(null);
                if (group != null) {
                    ProductPrice price = ProductPrice.builder()
                            .product(product)
                            .customerGroup(group)
                            .price(gp.getPrice())
                            .build();
                    priceRepository.save(price);
                }
            }
        }

        // Save images
        if (images != null) {
            for (MultipartFile file : images) {
                String filePath = saveFile(file, "images");
                if (filePath != null) {
                    ProductMedia media = ProductMedia.builder()
                            .product(product)
                            .filePath(filePath)
                            .mediaType(ProductMedia.MediaType.IMAGE)
                            .build();
                    product.getMedia().add(media);
                }
            }
        }

        // Save videos
        if (videos != null) {
            for (MultipartFile file : videos) {
                String filePath = saveFile(file, "videos");
                if (filePath != null) {
                    ProductMedia media = ProductMedia.builder()
                            .product(product)
                            .filePath(filePath)
                            .mediaType(ProductMedia.MediaType.VIDEO)
                            .build();
                    product.getMedia().add(media);
                }
            }
        }

        productRepository.save(product);
        return new ApiResponse(true, "Product added successfully", product.getId());
    }

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public ApiResponse getProductsForCustomer(User customer) {
        List<Product> products = productRepository.findByActiveTrue();
        Long groupId = customer.getCustomerGroup().getId();

        List<Map<String, Object>> productList = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("category", p.getCategory());
            map.put("sku", p.getSku());
            map.put("stockQuantity", p.getStockQuantity());
            map.put("media", p.getMedia());

            ProductPrice price = priceRepository.findByProductIdAndCustomerGroupId(p.getId(), groupId).orElse(null);
            map.put("price", price != null ? price.getPrice() : null);
            return map;
        }).collect(Collectors.toList());

        return new ApiResponse(true, "Products fetched", productList);
    }

    public ApiResponse deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return new ApiResponse(false, "Product not found");

        product.setActive(false);
        productRepository.save(product);
        return new ApiResponse(true, "Product removed successfully");
    }

    public ApiResponse updateProductPrices(Long productId, List<ProductRequest.GroupPrice> prices) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return new ApiResponse(false, "Product not found");

        for (ProductRequest.GroupPrice gp : prices) {
            ProductPrice existing = priceRepository.findByProductIdAndCustomerGroupId(productId, gp.getGroupId()).orElse(null);
            if (existing != null) {
                existing.setPrice(gp.getPrice());
                priceRepository.save(existing);
            } else {
                CustomerGroup group = groupRepository.findById(gp.getGroupId()).orElse(null);
                if (group != null) {
                    ProductPrice newPrice = ProductPrice.builder()
                            .product(product)
                            .customerGroup(group)
                            .price(gp.getPrice())
                            .build();
                    priceRepository.save(newPrice);
                }
            }
        }
        return new ApiResponse(true, "Prices updated successfully");
    }

    private String saveFile(MultipartFile file, String subDir) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, subDir, fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return "/uploads/" + subDir + "/" + fileName;
        } catch (IOException e) {
            return null;
        }
    }
}
