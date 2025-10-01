package com.skrt.wigellpadelservice.utility;

import com.skrt.wigellpadelservice.exceptions.BadRequestException;

import java.util.function.Supplier;

public class Validate {

    public Validate() {
    }

    public static<T> T notNull (T value, String field){
        if(value == null) throw new BadRequestException(field, " is required");
        return value;
    }

    public static void positive(Integer value, String field){
        if(value == null) throw new BadRequestException(field, " is required");
        if(value <= 0) throw new BadRequestException(field, "must be larger than 0", value);
    }

    public static void isTrue(boolean condition, Supplier<? extends RuntimeException> ex){
        if(!condition) throw ex.get();
    }
}
