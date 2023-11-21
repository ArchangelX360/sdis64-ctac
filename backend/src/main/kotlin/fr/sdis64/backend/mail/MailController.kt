package fr.sdis64.backend.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mailer")
class MailController(
    @Autowired private val webmailService: WebmailService,
) {
    @GetMapping(value = ["/unseen"])
    suspend fun getUnseenMailSubjects(): Set<String> = webmailService.getUnarchivedMails()
}
