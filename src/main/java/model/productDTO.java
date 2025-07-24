package model;

//final
public class productDTO {
	
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

	public productDTO(String productName, int productStock) {
        this.productName = productName;
        this.productStock = productStock;
    }

}