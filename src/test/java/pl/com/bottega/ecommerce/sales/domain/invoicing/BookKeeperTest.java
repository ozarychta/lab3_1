package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    BookKeeper bookKeeper;
    InvoiceRequest invoiceRequest0;
    InvoiceRequest invoiceRequest1;
    InvoiceRequest invoiceRequest2;
    ProductData productData;

    @Mock
    TaxPolicy taxPolicy;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        bookKeeper = new BookKeeper(new InvoiceFactory());
        productData = new ProductData(Id.generate(), new Money(BigDecimal.ONE),"product", ProductType.STANDARD, new Date());

        ClientData clientData = new ClientData(Id.generate(), "client");
        RequestItem item1 = new RequestItem(productData,10, new Money(new BigDecimal(100)));
        RequestItem item2 = new RequestItem(productData,20, new Money(new BigDecimal(200)));

        invoiceRequest0 = new InvoiceRequest(clientData);

        invoiceRequest1 = new InvoiceRequest(clientData);
        invoiceRequest1.add(item1);

        invoiceRequest2 = new InvoiceRequest(clientData);
        invoiceRequest2.add(item1);
        invoiceRequest2.add(item2);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(new BigDecimal(300)),""));

    }

    @Test
    public void issuance_invoiceRequestWithOneItem_shouldReturnInvoiceWithOneInvoiceLine() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest1,taxPolicy);
        assertThat(invoice.getItems().size(), is(1));
    }

    @Test
    public void issuance_invoiceRequestWithOneItem_shouldInvokeCalculateTaxOneTime() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest1,taxPolicy);
        verify(taxPolicy, times(1)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void issuance_invoiceRequestWithTwoItems_shouldReturnInvoiceWithTwoInvoiceLines() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest2,taxPolicy);
        assertThat(invoice.getItems().size(), is(2));
    }

    @Test
    public void issuance_invoiceRequestWithTwoItems_shouldInvokeCalculateTaxTwoTimes() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest2,taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void issuance_invoiceRequestWithZeroItems_shouldReturnInvoiceWithNoInvoiceLine() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest0,taxPolicy);
        assertThat(invoice.getItems().size(), is(0));
    }

    @Test
    public void issuance_invoiceRequestWithZeroItems_shouldInvokeCalculateTaxZeroTimes() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest0,taxPolicy);
        verify(taxPolicy, times(0)).calculateTax(any(ProductType.class), any(Money.class));
    }

}
