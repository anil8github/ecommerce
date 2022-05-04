package ecommerce.inventory.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.inventory.event.For;
import ecommerce.inventory.model.Inventory;
import ecommerce.inventory.service.InventoryManagementService;

@RestController
public class InventoryController {
	
	@Autowired
	private InventoryManagementService inventoryManagementService;
	
//	@PutMapping("/inventory")
//	public void createNewInventory(@RequestBody Inventory inventoryRequested) {
//		For for1 = new For("Product", inventoryRequested.getProductId());
//		inventoryManagementService.createNewInventory(inventoryRequested, for1);
//	}
	
	@PostMapping("/inventory/{productId}")
	public void updateInventory(@PathVariable("productId") String productId , @RequestBody Long quantity) {
		For for1 = new For("Product", productId);
		inventoryManagementService.updateInventory(productId, quantity, for1);
	}
	
	@GetMapping("/inventory/{productId}")
	public Inventory getInventory(@PathVariable("productId") String productId) {
		return inventoryManagementService.getInventory(productId);
	}
	
	@GetMapping("/inventories")
	public List<Inventory> getInventories() {
		return inventoryManagementService.getInventories();
	}

}
