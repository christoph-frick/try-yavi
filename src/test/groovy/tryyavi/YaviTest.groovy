package tryyavi

import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.core.ConstraintViolations
import am.ik.yavi.core.Validator
import spock.lang.Specification

// test the things from https://github.com/making/yavi#example
class YaviTest extends Specification {

    Validator<User> userValidator = ValidatorBuilder.of(User)
            .constraint({ it.name } as ValidatorBuilder.ToCharSequence /* cast sucks */, "name", {
                it.notBlank().lessThanOrEqual(20)
            })
            .constraint({ it.email } as ValidatorBuilder.ToCharSequence, "email", {
                it.notBlank().lessThanOrEqual(50).email()
            })
            .constraint({ it.age } as ValidatorBuilder.ToInteger, "age", {
                it.notNull().greaterThanOrEqual(0).lessThanOrEqual(200)
            })
            .build()

    def "simple"() {
        when:
        ConstraintViolations validUserResult = userValidator.validate(new User("Test", "test@example.com", 42))
        then:
        validUserResult.valid
        validUserResult.empty

        when:
        ConstraintViolations invalidUserResult = userValidator.validate(new User("Test", null, 2019))
        then:
        !invalidUserResult.valid
        invalidUserResult.size() == 2
        invalidUserResult[0].messageKey() == 'charSequence.notBlank'
    }

    Validator<Country> countryValidator = ValidatorBuilder.of(Country)
            .constraint({ it.name } as ValidatorBuilder.ToCharSequence, "name", {
                it.notBlank().lessThanOrEqual(20)
            })
            .build()

    Validator<City> cityValidator = ValidatorBuilder.of(City)
            .constraint({ it.name } as ValidatorBuilder.ToCharSequence, "name", {
                it.notBlank().lessThanOrEqual(20)
            })
            .build()

    Validator<Address> addressValidator = ValidatorBuilder.of(Address)
            .nest({ it.city }, "city", cityValidator)
            .nest({ it.country }, "country", countryValidator)
            .build()


    def "nested"() {
        when: "a valid address is used"
        def validAddressRessult = addressValidator.validate(new Address(new City("City"), new Country("Country")))
        then: "the result is valid"
        validAddressRessult.valid

        when: "no nested objects are set"
        def nullResult = addressValidator.validate(new Address())
        then: "there are null violations"
        ! nullResult.valid
        nullResult.size()==2
        nullResult*.name().toSet() == ['city', 'country'].toSet()
        nullResult*.messageKey().toSet() == ['object.notNull'].toSet()

        when: "there are errors in the nested objects"
        def invalidResult = addressValidator.validate(new Address(new City("Verylongnamethatisnotvalidatall"), new Country(/*null*/)))
        then: "the address is invalid"
        ! invalidResult.valid
        invalidResult.size()==2
        invalidResult[0].with{ [it.name(), it.messageKey() ] } == ['city.name', 'container.lessThanOrEqual']
        invalidResult[1].with{ [it.name(), it.messageKey() ] } == ['country.name', 'charSequence.notBlank']
    }

}
