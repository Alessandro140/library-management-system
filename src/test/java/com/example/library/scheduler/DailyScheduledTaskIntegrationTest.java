package com.example.library.scheduler;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test the scheduler update status")
@SpringBootTest
@Transactional
public class DailyScheduledTaskIntegrationTest {

    @Autowired
    private DailyScheduledTask scheduler;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("Test the scheduler update status successfully")
    @Test
    public void testSchedulerUpdatesLoanStatus() {
        // 1. Creo un utente di test
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(UserRole.ADMIN);
        user.setEmail("testuser@example.com");
        userRepository.save(user);

        Loan loanToday = new Loan();
        loanToday.setUser(user);
        loanToday.setLoanDate(LocalDate.now());
        loanToday.setDueDate(LocalDate.now());
        loanToday.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loanToday);

        Loan loanTomorrow = new Loan();
        loanTomorrow.setUser(user);
        loanTomorrow.setLoanDate(LocalDate.now());
        loanTomorrow.setDueDate(LocalDate.now().plusDays(1));
        loanTomorrow.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loanTomorrow);

        scheduler.changeLoanStatus();

        Loan updatedToday = loanRepository.findById(loanToday.getId()).orElseThrow();
        assertEquals(LoanStatus.LATE, updatedToday.getStatus());

        Loan updatedTomorrow = loanRepository.findById(loanTomorrow.getId()).orElseThrow();
        assertEquals(LoanStatus.ACTIVE, updatedTomorrow.getStatus());
    }

    @DisplayName("Should find no loan to update")
    @Test
    public void testSchedulerUpdatesLoanStatusNoLoan() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(UserRole.ADMIN);
        user.setEmail("testuser@example.com");
        userRepository.save(user);

        Loan loanTomorrow = new Loan();
        loanTomorrow.setUser(user);
        loanTomorrow.setLoanDate(LocalDate.now());
        loanTomorrow.setDueDate(LocalDate.now().plusDays(1));
        loanTomorrow.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loanTomorrow);

        scheduler.changeLoanStatus();

        Loan updatedTomorrow = loanRepository.findById(loanTomorrow.getId()).orElseThrow();
        assertEquals(LoanStatus.ACTIVE, updatedTomorrow.getStatus());
    }
}
