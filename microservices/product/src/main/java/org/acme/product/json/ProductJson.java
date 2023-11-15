package org.acme.product.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.product.data.entity.Product;
import org.acme.product.data.entity.Unit;
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

    public ProductJson(){}

    public ProductJson(String id, String code, String name, String imageUrl, BigDecimal price, BigDecimal cogs, Long stock, Long stockOnHold, Long stockOutstanding) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.cogs = cogs;
        this.stock = stock;
        this.stockOnHold = stockOnHold;
        this.stockOutstanding = stockOutstanding;
    }

    public static ProductJson fromEntity(Product product) {
        ProductJson json = new ProductJson(product.getId(),
                product.getCode(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                product.getCogs(),
                product.getStock(),
                product.getStockOnHold(),
                product.getStockOutstanding());
        if (product.getUnit() != null) {
            json.setUnitId(product.getUnit().getId());
            json.setUnitName(product.getUnit().getName());
        }
        return json;
    }

    public Product toEntity() {
        Product product = new Product(getId(), getCode(), getName(), getImageUrl(), getPrice(), getCogs(), getStock(), getStockOnHold(), getStockOutstanding());
        if (StringUtils.isNotBlank(getUnitId())) {
            product.setUnit(new Unit(getUnitId(), getUnitName()));
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

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
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
                '}';
    }
}
