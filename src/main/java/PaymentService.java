import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentService {
    /** доп. задание от Петра к экзамену на Spring https://pastebin.com/ZbR5EEPM*/

     private PaymentRepository paymentRepository;
     private FeeRepository feeRepository;
     private UserRepository userRepository;

     private NotificationRestClient notificationRestClient = new NotificationRestClient();
     private CbrRestClient cbrRestClient = new CbrRestClient();

     public Optional<User> findAuthorizedUser() {
         Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         var user = userRepository.findUserById(userId);
         return user;
     }

     public Optional<User> findUserById(int id) {
         return userRepository.findUserById(id);
     }

     public double getFeeKoeff(double amount) {
         if (amount < 1000) {
             return 0.015;
         }
         if (amount > 5000) {
            return 0.005;
         }
         return 0.01;
     }

     @Transactional
     public boolean processPayment(double amount, Currency currency, Long recipientId) {
         var userSrc = findAuthorizedUser();
         if (userSrc.isEmpty) {
             return false;
         }
         var userDst = findUserById(recipientId);
         if (userDsc.isEmpty) {
             return false;
         }
         double amountInRub = amount * cbrRestClient.doRequest().getRates().get(currency.getCode());
         double feeKoeff = getFeeKoeff(amount);

        userSrc.get().setBalance(userSrc.get().getBalance - amountInRub - amountInRub * feeKoeff);
        userDst.get().setBalance(userDst.get().getBalance + amountInRub);

        Payment payment = new Payment(amountInRub, findUser(), recipientId);
        paymentRepository.save(payment);

        Fee fee = new Fee(amount * feeKoeff, userSrc);
        feeRepository.save(fee);

        try {
            notificationRestClient.notify(payment);
        } catch (Throwable t) {
            return false;
        }

        return true;
     }

    /**
     @Transactional
     public Fee save(Predicate<> condition, int koeff, User user, double amount) {
     if (condition.test(amount)) {
     Fee fee = new Fee(amount * koeff, user);
     feeRepository.save(fee);

     }
     }
     */

}
