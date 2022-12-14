package br.com.equipe7.desafio_spring.service;

import br.com.equipe7.desafio_spring.dto.ProductPurchaseDTO;
import br.com.equipe7.desafio_spring.dto.PurchaseRequestDTO;
import br.com.equipe7.desafio_spring.dto.TicketResponseDTO;
import br.com.equipe7.desafio_spring.exception.EmptyRequestException;
import br.com.equipe7.desafio_spring.exception.InventoryException;
import br.com.equipe7.desafio_spring.exception.NotFoundException;
import br.com.equipe7.desafio_spring.exception.PurchaseWithInvalidQuantityException;
import br.com.equipe7.desafio_spring.service.interfaces.IPurchase;
import br.com.equipe7.desafio_spring.model.Product;
import br.com.equipe7.desafio_spring.model.Ticket;
import br.com.equipe7.desafio_spring.repository.ProductRepo;
import br.com.equipe7.desafio_spring.util.TicketNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseService implements IPurchase {

    @Autowired
    private ProductRepo productRepo;

    /**
     * Criação da requisição de compra
     * @author Gabriel
     * @param request uma requisição de compra apresentando uma lista de produtos a serem adquiridos
     * @return Um 'ticket' de compra, apresentado os produtos adquiridos e o valor total da compra
     */
    @Override
    public TicketResponseDTO purchase(PurchaseRequestDTO request) {
        if(request == null) {
            throw new EmptyRequestException("A requisição de compra não pode ser vazia");
        }

        List<Product> selectedProducts = getSelectedProducts(request);
        updateInventory(selectedProducts);

        return createTicket(selectedProducts);
    }

    /**
     * @author Gabriel
     * @param request uma requisição de compra apresentando uma lista de produtos a serem adquiridos
     * @return Uma lista dos produtos selecionados pela requisição
     */
    private List<Product> getSelectedProducts(PurchaseRequestDTO request) {
        List<ProductPurchaseDTO> articlesPurchaseRequest = request.getArticlesPurchaseRequest();
        List<Product> selectedProducts = new ArrayList<>();

        for(ProductPurchaseDTO productPurchase : articlesPurchaseRequest) {
            Product product = getProductByProductPurchaseValidator(productPurchase);
            product.setQuantity(productPurchase.getQuantity());
            selectedProducts.add(product);
        }

        return selectedProducts;
    }

    /**
     * Valor total da compra do produto
     * @author Gabriel
     * @param product O produto a ser adquirido
     * @return O valor total de um produto conforme o seu valor * quantidade informada
     */
    private BigDecimal getProductPrice(Product product) {
        int quantity = product.getQuantity();
        BigDecimal price = product.getPrice();
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Valor total da compra de produtos
     * @author Gabriel
     * @param products uma lista de produtos
     * @return O valor total do ticket a partir de uma lista de produtos
     */
    private BigDecimal getTotalTicket(List<Product> products) {
        BigDecimal totalTicket = BigDecimal.ZERO;

        for(Product product : products){
            BigDecimal totalProductPrice = getProductPrice(product);
            totalTicket = totalTicket.add(totalProductPrice);
        }

        return totalTicket;
    }

    /**
     * Criador de ticket de compra
     * @author Gabriel
     * @param products Uma lista de produtos que estarão presentes no ticket
     * @return Um ticket de compra, apresentado os produtos adquiridos e o valor total da compra
     */
    private TicketResponseDTO createTicket(List<Product> products) {
        int ticketId = TicketNumberGenerator.getInstance().getNext();
        return new TicketResponseDTO(new Ticket(ticketId, products, getTotalTicket(products)));
    }

    /**
     * validador do produto para compra
     * @author Gabriel
     * @param productPurchase Um produto de um objeto ser comprado, possuindo productId e quantity.
     * @return Um objeto do tipo Produto válido
     */
    private Product getProductByProductPurchaseValidator(ProductPurchaseDTO productPurchase) {
        Optional<Product> product =  this.productRepo.getProductById(productPurchase.getProductId());

        if(product.isEmpty()){
            throw new NotFoundException("Produto com id: "
                    + productPurchase.getProductId()
                    + " não encontrado");
        }

        if(productPurchase.getQuantity() <= 0) {
            throw new PurchaseWithInvalidQuantityException("O produto com id: "
                    + productPurchase.getProductId()
                    + " deve possuir uma quantidade válida" );
        }

        if(verifyInventory(product.get(), productPurchase.getQuantity())) {
            throw new InventoryException("Não há " + product.get().getName() +  " suficiente no estoque");
        }

        return product.get();
    }

    /**
     * valida se o inventário possui a quantidade exigida do produto
     * @author Gabriel
     * @param product Um produto a ser consultado
     * @param requiredQuantity A quantidade requisitada para compra
     * @return Uma resposta indicando se o inventário possui a quantidade exigida
     */
    private boolean verifyInventory(Product product, int requiredQuantity) {
        return requiredQuantity > product.getQuantity();
    }

    /**
     * Atualiza as quantidades de cada produto selecionado no estoque
     * @author Gabriel
     * @param selectedProducts uma lista dos produtos selecionados na compra
     */
    private void updateInventory(List<Product> selectedProducts){
        List<Product> productsFromInventory = this.productRepo.getAllProducts();

        for(Product productInventory : productsFromInventory){
            for(Product selectedProduct : selectedProducts){
                if(productInventory.getProductId() == selectedProduct.getProductId()) {
                    int newQuantityInInventory = productInventory.getQuantity() - selectedProduct.getQuantity();
                    productInventory.setQuantity(newQuantityInInventory);
                }
            }
        }

        this.productRepo.updateProducts(productsFromInventory);
    }


}
