package no.pipehill.reiseapp.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(proxyBeanMethods = false)
class ReiseappApplication

fun main(args: Array<String>) {
    runApplication<ReiseappApplication>(*args)
}
