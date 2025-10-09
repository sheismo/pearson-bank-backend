package com.zainab.PearsonBank.types;

public enum TransactionStatus {
    PROCESSING, // transaction in progress
    SUCCESSFUL, // initiated and successfully processed, saved to transaction table
    FAILED,  // initiated and couldn't process, saved to transaction table
    INCOMPLETE // initiated but failed e.g insufficient funds, not saved to transaction table
}
