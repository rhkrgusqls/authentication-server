package model;
//final
public class ProductDTO {




	private int productId;
	private String productName;
	private int productStock;

    public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getProductStock() {
		return productStock;
	}

	public void setProductStock(int productStock) {
		this.productStock = productStock;
	}

	public int getProductId() { return productId; }

	public void setProductId(int productId) { this.productId = productId; }

	public ProductDTO() {}

	public ProductDTO(int productId, String productName, int productStock) {
        this.productId = productId;
		this.productName = productName;
        this.productStock = productStock;
    }

}