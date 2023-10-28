package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dao.CheckoutRepository;
import com.luv2code.springbootlibrary.dao.HistoryRepository;
import com.luv2code.springbootlibrary.dao.PaymentRepository;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Checkout;
import com.luv2code.springbootlibrary.entity.History;
import com.luv2code.springbootlibrary.entity.Payment;
import com.luv2code.springbootlibrary.responsemodels.ShelfCurrentLoansResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class BookService {
    private BookRepository bookRepository;
    private CheckoutRepository checkoutRepository;
    private HistoryRepository historyRepository;
    private PaymentRepository paymentRepository;

    public BookService(BookRepository bookRepository,
                       CheckoutRepository checkoutRepository ,
                       HistoryRepository historyRepository,
                       PaymentRepository paymentRepository) {
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
    }
    public Book checkoutBook (String userEmail, Long bookId) throws Exception {
        Optional<Book> book = bookRepository.findById(bookId);
        Checkout validateCheckout  = checkoutRepository.findByUserEmailAndBookId(userEmail,bookId);
        if(!book.isPresent() || validateCheckout != null || book.get().getCopiesAvailable() <= 0){
            throw new Exception("Book doesn't exist or already checked by user");
        }

        List<Checkout> currentBooksCheckedOut = checkoutRepository.findBooksByUserEmail(userEmail);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean bookNeedsReturned = false;

        for(Checkout checkout : currentBooksCheckedOut){
            Date d1 = sdf.parse(checkout.getReturnDate());
            Date d2 = sdf.parse(LocalDate.now().toString());
            TimeUnit timeUnit = TimeUnit.DAYS;
            double differenceInTime = timeUnit.convert(d1.getTime()-d2.getTime(),TimeUnit.MILLISECONDS);
            if(differenceInTime < 0 ) {
                bookNeedsReturned = true;
                break;
            }
        }
        Payment userPayment = paymentRepository.findByUserEmail(userEmail);
        if((userPayment != null && userPayment.getAmount() > 0) || (userPayment != null && bookNeedsReturned)){
          throw new Exception("Outstanding fees");
        }
        if(userPayment == null){
            Payment payment = new Payment();
            payment.setAmount(00.00);
            payment.setUserEmail(userEmail);
            paymentRepository.save(payment);
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() - 1);
        bookRepository.save(book.get());

        Checkout checkout = new Checkout(
                userEmail,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(7).toString(),
                book.get().getId()
        );
        checkoutRepository.save(checkout);
        return  book.get();
    }
    public  Boolean checkoutBookByUser(String userEmail , Long bookId){
        Checkout validateCheckout  = checkoutRepository.findByUserEmailAndBookId(userEmail,bookId);
        if(validateCheckout != null){
            return true;
        }else {
            return false;
        }
    }
    public int currentloansCount(String userEmail){
        return checkoutRepository.findBooksByUserEmail(userEmail).size();
    }
    public List<ShelfCurrentLoansResponse> currentLoans (String userEmail) throws Exception {
        List<ShelfCurrentLoansResponse> shelfCurrentLoansResponses = new ArrayList<>();
        List<Checkout> checkoutList = checkoutRepository.findBooksByUserEmail(userEmail);
        List<Long> bookIdList = new ArrayList<>();

        for (Checkout checkout :  checkoutList ){
            bookIdList.add(checkout.getBookId());
        }

        List<Book> books = bookRepository.findBooksByBookIds(bookIdList);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for(Book book : books){
            Optional<Checkout> checkout = checkoutList.stream()
                    .filter(x-> x.getBookId() == book.getId()).findFirst();
            if(checkout.isPresent()){
                Date d1 = sdf.parse(checkout.get().getReturnDate());
                Date d2 = sdf.parse(LocalDate.now().toString());

                TimeUnit timeUnit = TimeUnit.DAYS;
                long difference_In_Time = timeUnit.convert(d1.getTime()-d2.getTime()
                        ,TimeUnit.MILLISECONDS);
                shelfCurrentLoansResponses.add(new ShelfCurrentLoansResponse(book,
                        (int) difference_In_Time));
            }
        }
        return shelfCurrentLoansResponses;

    }
    public void returnBook(String userEmail , long bookId) throws Exception {

        Optional<Book> book = bookRepository.findById(bookId);
        Checkout valiateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail,bookId);

        if(!book.isPresent() || valiateCheckout == null ){
            throw new Exception("Book doesn't exist or not CheckedOut By User");
        }
        book.get().setCopiesAvailable(book.get().getCopiesAvailable() + 1);
        bookRepository.save(book.get());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = sdf.parse(valiateCheckout.getReturnDate());
        Date d2 = sdf.parse(LocalDate.now().toString());
        TimeUnit timeUnit = TimeUnit.DAYS;
        double differenceInTime = timeUnit.convert(d1.getTime() - d2.getTime(),timeUnit.MILLISECONDS);
        if(differenceInTime < 0){
            Payment payment = paymentRepository.findByUserEmail(userEmail);
            payment.setAmount(payment.getAmount() + (differenceInTime * -1));
            paymentRepository.save(payment);
        }
        checkoutRepository.deleteById(valiateCheckout.getId());

        History history = new History(
                userEmail,
                valiateCheckout.getCheckoutDate(),
                LocalDate.now().toString(),
                book.get().getTitle(),
                book.get().getAuthor(),
                book.get().getDescription(),
                book.get().getImg()
        );
        historyRepository.save(history);
    }
    public void renewLoan(String userEmail , long bookId) throws Exception {
        Checkout valiateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail,bookId);
        if(valiateCheckout == null ){
            throw new Exception("Book doesn't exist or not CheckedOut By User");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = sdf.parse(valiateCheckout.getReturnDate());
        Date d2 = sdf.parse(LocalDate.now().toString());
        if(d1.compareTo(d2) > 0 || d1.compareTo(d2) ==0){
            valiateCheckout.setReturnDate(LocalDate.now().plusDays(7).toString());
            checkoutRepository.save(valiateCheckout);
        }

    }
}
