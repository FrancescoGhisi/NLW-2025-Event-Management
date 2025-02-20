package br.com.nlw.events.service;

import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicatorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repository.EventRepository;
import br.com.nlw.events.repository.SubscriptionRepository;
import br.com.nlw.events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {

        // recuperar o evento pelo nome
        Event event = eventRepository.findByPrettyName(eventName);
        if (event == null) { // caso alternativo 2
            throw new EventNotFoundException("Event " + eventName + " doesn't exist.");
        }
        User userRecovery = userRepository.findByEmail(user.getEmail());
        if (userRecovery == null) { // caso alternativo 1
            userRecovery = userRepository.save(user);
        }

        User indicator = userRepository.findById(userId).orElse(null);
        if (indicator == null) {
            throw new UserIndicatorNotFoundException("Indicator user " + userId + " doesn't exist.");
        }

        Subscription subscription = new Subscription();
        subscription.setEvent(event);
        subscription.setSubscriber(userRecovery);
        subscription.setIndication(indicator);

        Subscription tempSub = subscriptionRepository.findByEventAndSubscriber(event, userRecovery);
        if (tempSub != null) { // caso alternativo 3
            throw new SubscriptionConflictException("Already exists subscription for user " + userRecovery.getName() +
                    " in event " + event.getTitle());
        }

        Subscription subscription1 = subscriptionRepository.save(subscription);

        return new SubscriptionResponse(subscription1.getSubscriptionNumber(), "http://codecraft.com/" +
                subscription1.getEvent().getPrettyName() + "/" + subscription1.getSubscriber().getId());
    }
}
