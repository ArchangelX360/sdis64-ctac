package fr.sdis64.brain.auth

import fr.sdis64.api.Session
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/session")
class SecurityController {
    @GetMapping
    fun getSession(principal: Principal): Session = Session(principal.name)
}
