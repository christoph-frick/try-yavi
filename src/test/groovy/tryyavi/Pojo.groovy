package tryyavi

import groovy.transform.TupleConstructor

@TupleConstructor
class User {
    String name
    String email
    Integer age
}

@TupleConstructor
class Country {
    String name
}

@TupleConstructor
class City {
    String name
}

@TupleConstructor
class Address {
    City city
    Country country
}

