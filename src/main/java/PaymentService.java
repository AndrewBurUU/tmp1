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
     private UserService userService;
     private FeeService feeService;

     private NotificationRestClient notificationRestClient = new NotificationRestClient();
     private CbrRestClient cbrRestClient = new CbrRestClient();

     public Optional<User> findAuthorizedUser() {
         Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         var user = userService.findUserById(userId);
         return user;
     }

     public Optional<User> findUserById(Long id) {
         return userService.findUserById(id);
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
    public Payment save(Payment payment) {
         return paymentRepository.save(payment);
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
        save(payment);

        Fee fee = new Fee(amount * feeKoeff, userSrc);
        feeService.save(fee);

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
