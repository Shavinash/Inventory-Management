package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}


@Entity
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String sku;
    private String category;
    private int quantity;
    private double price;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

interface ProductRepo extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
}

@Service
class ProductService {
    @Autowired
    private ProductRepo repo;

    public Product addProduct(Product p) { return repo.save(p); }
    public List<Product> getAll() { return repo.findAll(); }

    public Product updateProduct(Long id, Product p) {
        Product existing = repo.findById(id).orElseThrow();
        existing.setName(p.getName());
        existing.setSku(p.getSku());
        existing.setCategory(p.getCategory());
        existing.setQuantity(p.getQuantity());
        existing.setPrice(p.getPrice());
        return repo.save(existing);
    }

    public void deleteProduct(Long id) { repo.deleteById(id); }
}

@RestController
@RequestMapping("/api/inventory")
class ProductController {
    @Autowired
    private ProductService service;

    @PostMapping("/add")
    public Product add(@RequestBody Product p) { return service.addProduct(p); }

    @GetMapping("/all")
    public List<Product> all() { return service.getAll(); }

    @PutMapping("/update/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product p) {
        return service.updateProduct(id, p);
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteProduct(id);
        return "Product deleted successfully";
    }
}
