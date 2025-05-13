package com.platform.credits

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PartnerCreditServiceApplication

fun main(args: Array<String>) {
    runApplication<PartnerCreditServiceApplication>(*args)
}
