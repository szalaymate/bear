# Bear: error handling experiments

This is a small webserver that serves an image of a **bear** built from three different **member**s:
* head
* body
* leg

Each *member* can be loaded from three different resources:
* working directory of java process
* a configured directory
* java resource packaged into running jar

Search order is respective to the list above.

The flow of data / error is illustrated in the following image:

![Data / Error Flow](DataErrorFlow.jpg)

## Run

```shell
./gradlew bootRun
```
This configures `specified-directory`, and uses `working-directory` as JVM working dir.

### Using special value `null` for error handling

Class: `BearWithNull`
Endpoint: `/bear-with-null/{head}/{body}/{leg}`

Errors are signaled as `null` value, and handled as `null` check in this solution.
`null` value is handled also well by SpringBoot as a return value of the controller method.

The possibility of `null` values doesn't appear in the signature of methods.

### Using exceptions

Class: `BearWithIOException`
Endpoint: `/bear-with-io-exception/{head}/{body}/{leg}`

Exceptions are propagated as they are - as `IOException`s.
All of them are handled by SpringBoot,
except that the image loading business logic in `readMember` uses them to load image from where it is possible.

Class: `BearWithException`
Endpoint: `/bear-with-exception/{head}/{body}/{leg}`

A custom exception, `NoSuchBearException` is propagated.
Handled specially by SpringBoot,
except that the image loading business logic in `readMember` uses them to load image from where it is possible.

The diagram is almost the same as the `IOException` case.
Since this custom exception is a `RuntimeException`, it does not appear in the signature of methods.

### Using `Optional`

Class: `BearWithOptional`
Endpoint: `/bear-with-optional/{head}/{body}/{leg}`

Errors are signaled as empty `Optional`s, and handled using its API in this solution.
`Optional` return value of controller methods is not handled by SpringBoot,
but it's possible to create `ResponseEntity` corresponding to the result.

### Using custom `Either` type

Class `BearWithEither`
Endpoint: `/bear-with-either/{head}/{body}/{leg}`

Errors are signaled as `Either` with some message included as an alternative.
A `ResponseEntity` is created as a result, based on the value of `Either`.
