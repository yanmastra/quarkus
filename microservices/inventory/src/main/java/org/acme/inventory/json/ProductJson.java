package org.acme.inventory.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.inventory.data.entity.Category;
import org.acme.inventory.data.entity.Product;
import org.acme.inventory.data.entity.Unit;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductJson {
    @JsonProperty("id")
    private String id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("cogs")
    private BigDecimal cogs;

    @JsonProperty("stock")
    private Long stock;

    @JsonProperty("stock_on_hold")
    private Long stockOnHold;
    @JsonProperty("stock_outstanding")
    private Long stockOutstanding;

    @JsonProperty("unit_id")
    private String unitId;
    @JsonProperty("unit_name")
    private String unitName;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("category_name")
    private String categoryName;

    public ProductJson(){}

    public ProductJson(Product product) {
        this.id = product.getId();
        this.code = product.getCode();
        this.name = product.getName();
        this.imageUrl = product.getImageUrl();
        this.price = product.getPrice();
        this.cogs = product.getCogs();
        this.stock = product.getStock();
        this.stockOnHold = product.getStockOnHold();
        this.stockOutstanding = product.getStockOutstanding();

        Unit uni = product.getUnit();
        if (uni != null) {
            this.unitId = uni.getId();
            this.unitName = uni.getName();
        }

        Category category = product.getCategory();
        if (category != null) {
            this.categoryId = category.getId();
            this.categoryName = category.getName();
        }
    }

    public static ProductJson fromEntity(Product product) {
        return new ProductJson(product);
    }

    public Product toEntity() {
        Product product = new Product(getId(), getCode(), getName(), getImageUrl(), getPrice(), getCogs(), getStock(), getStockOnHold(), getStockOutstanding());
        if (StringUtils.isNotBlank(getUnitId())) {
            product.setUnit(new Unit(getUnitId(), getUnitName()));
        }

        if (StringUtils.isNotBlank(getCategoryId())) {
            product.setCategory(new Category(getCategoryId(), getCategoryName()));
        }
        return product;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonIgnore
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @JsonIgnore
    public BigDecimal getCogs() {
        return cogs;
    }

    public void setCogs(BigDecimal cogs) {
        this.cogs = cogs;
    }

    @JsonIgnore
    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    @JsonIgnore
    public Long getStockOnHold() {
        return stockOnHold;
    }

    public void setStockOnHold(Long stockOnHold) {
        this.stockOnHold = stockOnHold;
    }

    @JsonIgnore
    public Long getStockOutstanding() {
        return stockOutstanding;
    }

    public void setStockOutstanding(Long stockOutstanding) {
        this.stockOutstanding = stockOutstanding;
    }

    @JsonIgnore
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    @JsonIgnore
    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @JsonIgnore
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @JsonIgnore
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "ProductJson{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", price=" + price +
                ", cogs=" + cogs +
                ", stock=" + stock +
                ", stockOnHold=" + stockOnHold +
                ", stockOutstanding=" + stockOutstanding +
                ", unitId='" + unitId + '\'' +
                ", unitName='" + unitName + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
