package com.example.library.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.library.service.BookService;
import com.example.library.service.LoanService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DailyScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(DailyScheduledTask.class);

    private final LoanService loanService;

    // Inietta il tuo service tramite costruttore
    public DailyScheduledTask(LoanService loanService) {
        this.loanService = loanService;
    }

    // Everyday at 20:00
    @Scheduled(cron = "0 0 20 * * *")
    public void changeLoanStatus() {

        try {
            this.loanService.updateLoanStatus();
        } catch (Exception e) {

        }
    }
}