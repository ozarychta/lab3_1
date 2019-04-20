package pl.com.bottega.ecommerce.sales.domain.client;

import pl.com.bottega.ddd.support.domain.BaseAggregateRoot;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.sales.domain.payment.Payment;
import pl.com.bottega.ecommerce.sales.domain.payment.PaymentFactory;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class Client extends BaseAggregateRoot{
private String name;

	public Client() {
	}

	private PaymentFactory paymentFactory;
	
	public ClientData generateSnapshot(){
		return new ClientData(id, name);
	}

	public boolean canAfford(Money amount) {		
		return true;//TODO explore domain rules ex: credit limit
	}

	/**
	 * Sample model: one aggregate creates another.<br>
	 * Client model does not compose Payment - therefore it does not manage Payment lifecycle.<br>
	 * Application layer is responsible for managing Payment lifecycle;
	 * 
	 * @param amount
	 * @return
	 */
	public Payment charge(Money amount) {
		if (! canAfford(amount)){			
			domainError("Can not afford: " + amount);
		}
		
		return paymentFactory.createPayment(generateSnapshot(), amount);
	}
}
