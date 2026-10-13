package com.servicesphere.config;

import com.servicesphere.assignment.AgentProfile;
import com.servicesphere.assignment.AgentProfileRepository;
import com.servicesphere.audit.AuditService;
import com.servicesphere.cases.*;
import com.servicesphere.customer.Account;
import com.servicesphere.customer.AccountRepository;
import com.servicesphere.customer.Contact;
import com.servicesphere.customer.ContactRepository;
import com.servicesphere.knowledge.KnowledgeArticle;
import com.servicesphere.knowledge.KnowledgeArticleRepository;
import com.servicesphere.notification.NotificationService;
import com.servicesphere.notification.NotificationType;
import com.servicesphere.security.Role;
import com.servicesphere.sla.SlaPolicy;
import com.servicesphere.sla.SlaPolicyRepository;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final CaseRepository caseRepository;
    private final CaseCommentRepository commentRepository;
    private final CaseHistoryRepository historyRepository;
    private final KnowledgeArticleRepository knowledgeRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    public DevDataSeeder(
            UserRepository userRepository,
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            AgentProfileRepository agentProfileRepository,
            SlaPolicyRepository slaPolicyRepository,
            CaseRepository caseRepository,
            CaseCommentRepository commentRepository,
            CaseHistoryRepository historyRepository,
            KnowledgeArticleRepository knowledgeRepository,
            NotificationService notificationService,
            AuditService auditService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.caseRepository = caseRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemoData) {
            log.info("Demo data seeding disabled via configuration");
            return;
        }

        if (userRepository.existsByEmail("admin@servicesphere.io")) {
            userRepository.findByEmail("admin@servicesphere.io").ifPresent(u -> {
                u.setFirstName("Vivek");
                u.setLastName("");
                userRepository.save(u);
            });
            userRepository.findByEmail("agent.sarah@servicesphere.io").ifPresent(u -> {
                u.setFirstName("Sarah");
                u.setLastName("");
                userRepository.save(u);
            });
            userRepository.findByEmail("john.doe@acme.com").ifPresent(u -> {
                u.setFirstName("John");
                u.setLastName("");
                u.setEmail("john@acme.com");
                userRepository.save(u);
            });
            log.info("Demo data already seeded. Synced names for Vivek, Sarah, and John.");
            return;
        }

        log.info("--- Seeding Deterministic Demo Data for ServiceSphere ---");

        // 1. SLA Policies
        slaPolicyRepository.save(new SlaPolicy(CasePriority.CRITICAL, 1, 4, "Critical business outage: 1h response, 4h resolution"));
        slaPolicyRepository.save(new SlaPolicy(CasePriority.HIGH, 4, 12, "High impact issue: 4h response, 12h resolution"));
        slaPolicyRepository.save(new SlaPolicy(CasePriority.MEDIUM, 8, 24, "Medium standard issue: 8h response, 24h resolution"));
        slaPolicyRepository.save(new SlaPolicy(CasePriority.LOW, 24, 72, "Low general inquiry: 24h response, 72h resolution"));

        // 2. Administrator
        User admin = new User("admin@servicesphere.io", passwordEncoder.encode("Admin@12345"), "Vivek", "", Role.ADMIN);
        admin = userRepository.save(admin);

        // 3. Support Agents
        User sarah = new User("agent.sarah@servicesphere.io", passwordEncoder.encode("Agent@12345"), "Sarah", "", Role.AGENT);
        sarah = userRepository.save(sarah);
        agentProfileRepository.save(new AgentProfile(sarah, "TECHNICAL,BILLING", 10));

        User marcus = new User("agent.marcus@servicesphere.io", passwordEncoder.encode("Agent@12345"), "Marcus", "Reynolds", Role.AGENT);
        marcus = userRepository.save(marcus);
        agentProfileRepository.save(new AgentProfile(marcus, "ACCOUNT,GENERAL", 8));

        User elena = new User("agent.elena@servicesphere.io", passwordEncoder.encode("Agent@12345"), "Elena", "Rostova", Role.AGENT);
        elena = userRepository.save(elena);
        agentProfileRepository.save(new AgentProfile(elena, "TECHNICAL,FEATURE_REQUEST", 12));

        // 4. Accounts & Contacts (Customers)
        Account acme = new Account("Acme Corporation", "Enterprise Software", "ENTERPRISE");
        acme.setWebsite("https://acmewidgets.com");
        acme.setPhone("+1-555-0199");
        acme = accountRepository.save(acme);

        User customer1 = new User("john@acme.com", passwordEncoder.encode("Customer@12345"), "John", "", Role.CUSTOMER);
        customer1 = userRepository.save(customer1);
        contactRepository.save(new Contact(customer1, acme, "VP of Engineering", "+1-555-0101"));

        Account cloudscale = new Account("CloudScale Technologies", "Cloud Infrastructure", "GROWTH");
        cloudscale.setWebsite("https://cloudscale.io");
        cloudscale.setPhone("+1-555-0288");
        cloudscale = accountRepository.save(cloudscale);

        User customer2 = new User("alice.chen@cloudscale.io", passwordEncoder.encode("Customer@12345"), "Alice", "Chen", Role.CUSTOMER);
        customer2 = userRepository.save(customer2);
        contactRepository.save(new Contact(customer2, cloudscale, "Lead DevOps Architect", "+1-555-0202"));

        Account apex = new Account("Apex Logistics", "Supply Chain", "STANDARD");
        apex.setWebsite("https://apexlogistics.com");
        apex.setPhone("+1-555-0377");
        apex = accountRepository.save(apex);

        User customer3 = new User("bob.miller@apexlogistics.com", passwordEncoder.encode("Customer@12345"), "Bob", "Miller", Role.CUSTOMER);
        customer3 = userRepository.save(customer3);
        contactRepository.save(new Contact(customer3, apex, "IT Operations Manager", "+1-555-0303"));

        // 5. Realistic Support Cases
        Instant now = Instant.now();

        // Case 1: In Progress, Billing, Critical, Assigned to Sarah
        SupportCase case1 = new SupportCase();
        case1.setCaseNumber("CASE-202610-0001");
        case1.setTitle("Enterprise Annual Renewal Duplicate Charge");
        case1.setDescription("Our finance team noticed our corporate credit card was billed twice for our Enterprise annual contract ($48,000 x 2). Please issue an immediate refund for invoice INV-9842.");
        case1.setCategory(CaseCategory.BILLING);
        case1.setPriority(CasePriority.CRITICAL);
        case1.setStatus(CaseStatus.IN_PROGRESS);
        case1.setCustomer(customer1);
        case1.setAssignedAgent(sarah);
        case1.setFirstResponseDeadline(now.minus(Duration.ofHours(2)));
        case1.setResolutionDeadline(now.plus(Duration.ofHours(3)));
        case1.setFirstRespondedAt(now.minus(Duration.ofHours(1)));
        case1 = caseRepository.save(case1);

        commentRepository.save(new CaseComment(case1, sarah, "Investigating this with our payments gateway provider. The duplicate authorization code is TXN-88491.", true));
        commentRepository.save(new CaseComment(case1, sarah, "Hello John, we have identified the secondary hold and initiated a reversal. You will see the credit back within 1-2 business days.", false));
        historyRepository.save(new CaseHistory(case1, customer1, HistoryAction.CREATED, null, "OPEN", "Support case logged"));
        historyRepository.save(new CaseHistory(case1, sarah, HistoryAction.ASSIGNED, "UNASSIGNED", "Sarah", "Rule-based skill match on BILLING"));
        historyRepository.save(new CaseHistory(case1, sarah, HistoryAction.STATUS_CHANGED, "OPEN", "IN_PROGRESS", "Agent began investigation"));

        // Case 2: Open, Technical, High, Auto-assigned to Elena
        SupportCase case2 = new SupportCase();
        case2.setCaseNumber("CASE-202610-0002");
        case2.setTitle("Okta SAML 2.0 Single Sign-On Returning 403 Forbidden");
        case2.setDescription("After rotating our X.509 certificate in Okta, our employees are receiving an HTTP 403 response when trying to login via SSO. Urgent assistance requested.");
        case2.setCategory(CaseCategory.TECHNICAL);
        case2.setPriority(CasePriority.HIGH);
        case2.setStatus(CaseStatus.OPEN);
        case2.setCustomer(customer2);
        case2.setAssignedAgent(elena);
        case2.setFirstResponseDeadline(now.plus(Duration.ofHours(3)));
        case2.setResolutionDeadline(now.plus(Duration.ofHours(11)));
        case2 = caseRepository.save(case2);

        historyRepository.save(new CaseHistory(case2, customer2, HistoryAction.CREATED, null, "OPEN", "Support case logged"));
        historyRepository.save(new CaseHistory(case2, elena, HistoryAction.ASSIGNED, "UNASSIGNED", "Elena Rostova", "Rule-based skill match on TECHNICAL"));

        // Case 3: Waiting for Customer, Account, Medium, Assigned to Marcus
        SupportCase case3 = new SupportCase();
        case3.setCaseNumber("CASE-202610-0003");
        case3.setTitle("Request to add secondary administrator email domain");
        case3.setDescription("We recently acquired a subsidiary (@logistics-direct.com) and need to allow users from this domain to join our company workspace.");
        case3.setCategory(CaseCategory.ACCOUNT);
        case3.setPriority(CasePriority.MEDIUM);
        case3.setStatus(CaseStatus.WAITING_FOR_CUSTOMER);
        case3.setCustomer(customer3);
        case3.setAssignedAgent(marcus);
        case3.setFirstResponseDeadline(now.minus(Duration.ofHours(10)));
        case3.setResolutionDeadline(now.plus(Duration.ofHours(14)));
        case3.setFirstRespondedAt(now.minus(Duration.ofHours(8)));
        case3 = caseRepository.save(case3);

        commentRepository.save(new CaseComment(case3, marcus, "Hello Bob, could you please provide a signed letter of authorization or DNS TXT verification token for the new domain?", false));
        historyRepository.save(new CaseHistory(case3, customer3, HistoryAction.CREATED, null, "OPEN", "Support case logged"));
        historyRepository.save(new CaseHistory(case3, marcus, HistoryAction.STATUS_CHANGED, "IN_PROGRESS", "WAITING_FOR_CUSTOMER", "Waiting for customer DNS verification"));

        // Case 4: Breached & Escalated Example, Technical, Critical
        SupportCase case4 = new SupportCase();
        case4.setCaseNumber("CASE-202610-0004");
        case4.setTitle("Production Webhook Delivery Pipeline Degradation");
        case4.setDescription("Over 30% of our real-time shipment events are failing with delivery timeouts. This is affecting downstream logistics operations.");
        case4.setCategory(CaseCategory.TECHNICAL);
        case4.setPriority(CasePriority.CRITICAL);
        case4.setStatus(CaseStatus.IN_PROGRESS);
        case4.setCustomer(customer3);
        case4.setAssignedAgent(elena);
        case4.setFirstResponseDeadline(now.minus(Duration.ofHours(6)));
        case4.setResolutionDeadline(now.minus(Duration.ofHours(2))); // Past deadline
        case4.setFirstRespondedAt(now.minus(Duration.ofHours(5)));
        case4.setSlaEscalated(true);
        case4 = caseRepository.save(case4);

        commentRepository.save(new CaseComment(case4, elena, "Root cause identified: upstream proxy pool latency spike. Working with network team.", true));
        historyRepository.save(new CaseHistory(case4, customer3, HistoryAction.CREATED, null, "OPEN", "Support case logged"));
        historyRepository.save(new CaseHistory(case4, elena, HistoryAction.ESCALATED, "FALSE", "TRUE", "Resolution deadline breached at " + now.minus(Duration.ofHours(2))));

        // Case 5: Resolved, Feature Request, Low
        SupportCase case5 = new SupportCase();
        case5.setCaseNumber("CASE-202610-0005");
        case5.setTitle("Export Analytics Data to Parquet / CSV format");
        case5.setDescription("Can we get an automated monthly CSV export of our case metrics sent to an S3 bucket?");
        case5.setCategory(CaseCategory.FEATURE_REQUEST);
        case5.setPriority(CasePriority.LOW);
        case5.setStatus(CaseStatus.RESOLVED);
        case5.setCustomer(customer1);
        case5.setAssignedAgent(sarah);
        case5.setFirstResponseDeadline(now.minus(Duration.ofDays(2)));
        case5.setResolutionDeadline(now.minus(Duration.ofHours(12)));
        case5.setFirstRespondedAt(now.minus(Duration.ofDays(2)));
        case5.setResolvedAt(now.minus(Duration.ofHours(10)));
        case5 = caseRepository.save(case5);

        commentRepository.save(new CaseComment(case5, sarah, "Hi John, this has been enabled in your dashboard under Settings > Data Export > Scheduled S3 sync.", false));
        historyRepository.save(new CaseHistory(case5, customer1, HistoryAction.CREATED, null, "OPEN", "Support case logged"));
        historyRepository.save(new CaseHistory(case5, sarah, HistoryAction.RESOLVED, "IN_PROGRESS", "RESOLVED", "Feature enabled and documented"));

        // Case 6: Closed, General, Low
        SupportCase case6 = new SupportCase();
        case6.setCaseNumber("CASE-202610-0006");
        case6.setTitle("Password reset email not received");
        case6.setDescription("Junior developer was unable to receive password reset link on their @acme.com inbox.");
        case6.setCategory(CaseCategory.GENERAL);
        case6.setPriority(CasePriority.LOW);
        case6.setStatus(CaseStatus.CLOSED);
        case6.setCustomer(customer1);
        case6.setAssignedAgent(marcus);
        case6.setFirstResponseDeadline(now.minus(Duration.ofDays(5)));
        case6.setResolutionDeadline(now.minus(Duration.ofDays(3)));
        case6.setFirstRespondedAt(now.minus(Duration.ofDays(5)));
        case6.setResolvedAt(now.minus(Duration.ofDays(4)));
        case6.setClosedAt(now.minus(Duration.ofDays(3)));
        case6 = caseRepository.save(case6);

        // Update agent caseload counts in profiles
        agentProfileRepository.findByUserId(sarah.getId()).ifPresent(p -> { p.setCurrentActiveCases(1); agentProfileRepository.save(p); });
        agentProfileRepository.findByUserId(marcus.getId()).ifPresent(p -> { p.setCurrentActiveCases(1); agentProfileRepository.save(p); });
        agentProfileRepository.findByUserId(elena.getId()).ifPresent(p -> { p.setCurrentActiveCases(2); agentProfileRepository.save(p); });

        // 6. Knowledge Articles
        knowledgeRepository.save(new KnowledgeArticle(
                "Configuring SAML 2.0 Single Sign-On (SSO) with Okta and Azure AD",
                "Step-by-step instructions for establishing identity provider metadata, ACS URLs, and X.509 certificate rotations.",
                "### Overview\nServiceSphere supports enterprise SAML 2.0 authentication.\n\n### Step 1: Export Metadata\nNavigate to Admin > SSO Settings and download your SP Metadata XML.\n\n### Step 2: Configure IdP\n- Entity ID: `https://servicesphere.io/auth/saml/metadata`\n- Assertion Consumer Service (ACS) URL: `https://servicesphere.io/auth/saml/sso`\n- NameID Format: `EmailAddress`\n\n### Step 3: Certificate Rotation\nEnsure new X.509 certificates are uploaded 48 hours prior to expiration to avoid HTTP 403 verification failures.",
                "TECHNICAL",
                "sso, saml, okta, azure, security",
                true,
                sarah
        ));

        knowledgeRepository.save(new KnowledgeArticle(
                "Understanding Enterprise Billing and Invoicing Cycles",
                "Explanation of annual recurring billing, seat expansion prorations, and invoice reconciliation.",
                "### Invoicing Frequency\nEnterprise plans are billed annually in advance. Overages and seat expansions are billed monthly on arrears.\n\n### Resolving Duplicate Charges\nIf a dual pre-authorization hold appears on your corporate card, our billing engine automatically releases secondary holds within 24 hours.",
                "BILLING",
                "billing, invoice, payment, refund, credit card",
                true,
                sarah
        ));

        knowledgeRepository.save(new KnowledgeArticle(
                "Troubleshooting REST API Rate Limits (HTTP 429)",
                "Best practices for API token quota management, exponential backoff, and webhook events.",
                "### Rate Limit Tiers\n- Standard: 1,000 requests / minute\n- Enterprise: 10,000 requests / minute\n\n### Response Headers\nWhen throttled, inspect the following response headers:\n- `X-RateLimit-Limit`\n- `X-RateLimit-Remaining`\n- `Retry-After` (seconds to pause before retry)\n\n### Recommended Implementation\nUse jittered exponential backoff when handling HTTP 429 status codes.",
                "TECHNICAL",
                "api, rate limit, 429, rest, throttling",
                true,
                elena
        ));

        knowledgeRepository.save(new KnowledgeArticle(
                "Managing User Roles & Access Control (RBAC)",
                "Guide for administrators on configuring ADMIN, AGENT, and CUSTOMER roles.",
                "### Role Hierarchy\n- **ADMIN**: Complete platform governance, agent configuration, SLA policy rules, and system audit logs.\n- **AGENT**: Case triage, customer replies, internal notes, and knowledge article publishing.\n- **CUSTOMER**: Secure portal access to create support cases, review activity timeline, and access published FAQs.",
                "ACCOUNT",
                "rbac, roles, permissions, admin, security",
                true,
                marcus
        ));

        knowledgeRepository.save(new KnowledgeArticle(
                "[Draft] High-Throughput Webhook Event Streaming Guide",
                "Architecture guide on configuring HMAC signature verification and resilient webhook consumers.",
                "Draft content under internal engineering review.",
                "TECHNICAL",
                "webhooks, events, streaming, draft",
                false, // Draft
                elena
        ));

        // 7. Seed Notifications
        notificationService.createNotification(customer1, "Case Status Update", "Your case CASE-202610-0001 status changed to IN_PROGRESS", NotificationType.STATUS_CHANGE, case1.getId(), "CASE");
        notificationService.createNotification(sarah, "New High-Priority Case", "You have been assigned to case CASE-202610-0001", NotificationType.CASE_ASSIGNMENT, case1.getId(), "CASE");
        notificationService.createNotification(admin, "SLA Breach Alert", "Case CASE-202610-0004 has breached SLA resolution deadline", NotificationType.SLA_BREACH, case4.getId(), "CASE");

        // 8. Seed Audit Log
        auditService.recordEvent(admin, "SYSTEM_BOOTSTRAP", "SYSTEM", "0", "Deterministic demo data seeded successfully", "127.0.0.1");

        log.info("--- Demo Data Seeding Completed Successfully ---");
    }
}
