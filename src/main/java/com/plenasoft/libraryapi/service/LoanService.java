package com.plenasoft.libraryapi.service;

import com.plenasoft.libraryapi.api.dto.LoanDTO;
import com.plenasoft.libraryapi.model.entity.Loan;

public interface LoanService {
    Loan save(Loan loan);
}
