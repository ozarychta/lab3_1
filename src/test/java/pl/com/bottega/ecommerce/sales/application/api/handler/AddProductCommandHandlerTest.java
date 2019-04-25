package pl.com.bottega.ecommerce.sales.application.api.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.system.application.SystemContext;
import pl.com.bottega.ecommerce.system.application.SystemUser;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AddProductCommandHandlerTest {

    AddProductCommandHandler addProductCommandHandler;
    AddProductCommand addProductCommand;
    Client client;
    Reservation reservation;
    SystemContext systemContext;
    Product availableProduct;
    Product unavailableProduct;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    SuggestionService suggestionService;

    @Mock
    ClientRepository clientRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        systemContext = new SystemContext();
        addProductCommand = new AddProductCommand(Id.generate(), Id.generate(), 10);
        client = new Client();
        reservation = new Reservation(Id.generate(), Reservation.ReservationStatus.OPENED, new ClientData(Id.generate(), "client"), new Date());
        availableProduct = new Product(Id.generate(), new Money(BigDecimal.ONE, Currency.getInstance("EUR")), "availableProduct", ProductType.STANDARD);
        unavailableProduct = new Product(Id.generate(), new Money(BigDecimal.ONE, Currency.getInstance("EUR")), "unavailableProduct", ProductType.STANDARD);
        unavailableProduct.markAsRemoved();
        addProductCommandHandler = new AddProductCommandHandler(reservationRepository, productRepository, suggestionService, clientRepository, systemContext);

        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);
        when(productRepository.load(any(Id.class))).thenReturn(availableProduct);
        when(clientRepository.load(any(Id.class))).thenReturn(client);
        when(suggestionService.suggestEquivalent(any(Product.class), any(Client.class))).thenReturn(new Product());
        doNothing().when(reservationRepository).save(any(Reservation.class));
    }

    @Test
    public void handle_productIsAvailable_shouldNotCallSuggestEquivalentMethod() {
        addProductCommandHandler.handle(addProductCommand);
        verify(suggestionService, never()).suggestEquivalent(availableProduct, client);
    }

    @Test
    public void handle_productIsUnavailable_shouldCallSuggestEquivalentMethodOnce() {
        when(productRepository.load(any(Id.class))).thenReturn(unavailableProduct);

        addProductCommandHandler.handle(addProductCommand);
        verify(suggestionService, times(1)).suggestEquivalent(unavailableProduct, client);
    }

    @Test
    public void handle_productIsAvailable_shouldAddThatProductToReservation() {
        addProductCommandHandler.handle(addProductCommand);
        assertThat(reservation.contains(availableProduct), is(true));
    }

    @Test
    public void handle_productIsUnavailable_shouldNotAddThatProductToReservation() {
        when(productRepository.load(any(Id.class))).thenReturn(unavailableProduct);

        addProductCommandHandler.handle(addProductCommand);
        assertThat(reservation.contains(unavailableProduct), is(false));
    }
}
