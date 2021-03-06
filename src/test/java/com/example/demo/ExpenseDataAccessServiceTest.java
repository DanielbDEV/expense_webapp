package com.example.demo;

import com.example.demo.dao.ExpenseDataAccessService;
import com.example.demo.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest//(classes = ExpenseDataAccessService.class)
@ActiveProfiles("test")
public class ExpenseDataAccessServiceTest {

    // enabling @Autowired resolved the NullPointerException with underTest in the tests
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExpenseDataAccessService underTest;

    @BeforeEach
    public void setUp() {
        //jdbcTemplate = new JdbcTemplate();
        underTest = new ExpenseDataAccessService(jdbcTemplate);
    }

    @Test
    public void canPerformCrud() {

        // Given Expense: name "Rewe"
        UUID idOne = UUID.randomUUID();
        UUID useridOne = UUID.randomUUID();
        Expense expenseOne = new Expense(idOne, "Rewe", 10, useridOne, LocalDate.parse("2020-04-01"));

        // Given Expense: name "Lidl"
        UUID idTwo = UUID.randomUUID();
        UUID useridTwo = UUID.randomUUID();
        Expense expenseTwo = new Expense(idTwo, "Lidl", 5, useridTwo, LocalDate.parse("2020-03-01"));

        // Insert into db
        underTest.insertExpense(idOne, expenseOne);
        underTest.insertExpense(idTwo, expenseTwo);

        // Select "Rewe" from db
        assertThat(underTest.selectExpenseById(idOne))
                .isNotNull()
                .isEqualToComparingFieldByField(expenseOne);

        // Select "Lidl" from db
        assertThat(underTest.selectExpenseById(idTwo))
                .isNotNull()
                .isEqualToComparingFieldByField(expenseTwo);

        // Select all Expenses from db
        List<Expense> expenses = underTest.selectAllExpenses();

        // Size of List should be 2 and both entries "Rewe" and "Lidl" should be included
        assertThat(expenses)
                .hasSize(2)
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expenseOne, expenseTwo);

        // Updated expense with name "Aldi" instead of "Rewe"
        Expense expenseUpdate = new Expense(idOne, "Aldi", 10, useridOne, LocalDate.parse("2020-04-01"));

        // Update expense in db
        assertThat(underTest.updateExpenseById(idOne, expenseUpdate)).isEqualTo(1);

        // Select updated Expense from db which should have the name "Aldi" instead of "Rewe"
        assertThat(underTest.selectExpenseById(idOne).getName())
                .isNotNull()
                .isEqualTo("Aldi");

        // Delete updated Expense with name "Aldi" from db
        assertThat(underTest.deleteExpenseById(idOne)).isEqualTo(1);

        /////////////////////////////////////////////////////

        // Get expenseOne by Id, which should not exist
        /*
        ERROR HERE:
        because it uses JdbcTemplate.queryForObject which only accept exactly one row in return. If no element exists, or if many elements exist, it throws an error!
        */

        ////////////////////////////////////
        /* New handling (Optionals)

        //expect
        Throwable thrown = catchThrowable(() -> underTest.selectExpenseById(idOne));

        //when
        assertThat(thrown).isInstanceOf(ExpenseNotFoundException.class)
                .hasNoCause()
                .withFailMessage("Expense ID not found");

         */
        ////////////////////////////////////

        /*
        Old handling of Errors, where we would not have a CustomGlobalExceptionHandler + ExpenseNotFoundException
         */
        //assertThat(underTest.selectExpenseById(idOne)).isEmpty();
        assertThat(underTest.selectExpenseById(idOne)).isNull();

        //////////////////////////////////////

        // Select all expenses from db, only expense with name "Lidl" should exist
        assertThat(underTest.selectAllExpenses())
                .hasSize(1)
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expenseTwo);
    }
/*
    @Test
    public void willReturn0IfNoExpenseFoundToGet() {
        // Given
        UUID id = UUID.fromString("");
        Expense expense = new Expense(id,"Hornbach not in Db", 99.99, UUID.randomUUID(), LocalDate.parse("9999-01-01"));

        // When
        Optional<Expense> updateResult = underTest.selectExpenseById(id);

        // Then
        assertThat(updateResult).isEmpty();
    }
*/
    @Test
    public void willReturn0IfNoExpenseFoundToDelete() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        int deleteResult = underTest.deleteExpenseById(id);

        // Then
        assertThat(deleteResult).isEqualTo(0);
    }

    @Test
    public void willReturn0IfNoExpenseFoundToUpdate() {
        // Given
        UUID id = UUID.randomUUID();
        Expense expense = new Expense(id,"Hornbach not in Db", 99.99, UUID.randomUUID(), LocalDate.parse("9999-01-01"));

        // When
        int updateResult = underTest.updateExpenseById(id, expense);

        // Then
        assertThat(updateResult).isEqualTo(0);
    }
}
