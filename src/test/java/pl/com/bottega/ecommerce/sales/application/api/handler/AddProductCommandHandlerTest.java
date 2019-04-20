package pl.com.bottega.ecommerce.sales.application.api.handler;

import org.junit.Before;
import org.junit.Test;
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
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.system.application.SystemContext;
import pl.com.bottega.ecommerce.system.application.SystemUser;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AddProductCommandHandlerTest {

    AddProductCommandHandler addProductCommandHandler;
    AddProductCommand addProductCommand;
    Client client;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    SuggestionService suggestionService;

    @Mock
    ClientRepository clientRepository;

    @Mock
    SystemContext systemContext;

    @Mock
    Reservation reservation;

    @Mock
    Product availableProduct;

    @Mock
    Product unavailableProduct;

    @Mock
    SystemUser systemUser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addProductCommandHandler = new AddProductCommandHandler(reservationRepository, productRepository, suggestionService, clientRepository, systemContext);

        addProductCommand = new AddProductCommand(Id.generate(), Id.generate(), 10);

        ClientData clientData = new ClientData(Id.generate(), "client");
//        Reservation reservation = new Reservation(Id.generate(), Reservation.ReservationStatus.OPENED, clientData, new Date());
        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);

        when(availableProduct.isAvailable()).thenReturn(true);
        when(unavailableProduct.isAvailable()).thenReturn(false);
        when(productRepository.load(any(Id.class))).thenReturn(availableProduct);

        when(systemUser.getClientId()).thenReturn(Id.generate());
        when(systemContext.getSystemUser()).thenReturn(systemUser);

        client = new Client();
        when(clientRepository.load(any(Id.class))).thenReturn(client);

        when(suggestionService.suggestEquivalent(any(Product.class), any(Client.class))).thenReturn(new Product());

        doNothing().when(reservationRepository).save(any(Reservation.class));
        doNothing().when(reservation).add(any(Product.class), anyInt());

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
}