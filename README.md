# Portfolio Contact Form Backend

A small Spring Boot API that powers the contact form on
santhoshkumar-dev-portfolio.netlify.app. It validates submissions,
protects against spam with a honeypot field, and sends two emails:
one notifying you, one auto-replying to the sender.

## Endpoint

```
POST /api/contact
Content-Type: application/json

{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "message": "Loved your portfolio, let's talk!",
  "website": ""
}
```

Success response:
```json
{ "success": true, "message": "Message sent successfully!" }
```

## 1. Get a Gmail App Password

Regular Gmail passwords won't work with SMTP. Generate an App Password:
1. Go to https://myaccount.google.com/apppasswords
2. Create one for "Mail"
3. Copy the 16-character password it gives you

## 2. Set environment variables

Locally, export these before running (or use an `.env` loader / your IDE's run config):

```bash
export MAIL_USERNAME=youraddress@gmail.com
export MAIL_PASSWORD=your16charapppassword
export OWNER_EMAIL=youraddress@gmail.com
export ALLOWED_ORIGINS=http://localhost:4200,https://santhoshkumar-dev-portfolio.netlify.app
```

## 3. Run it locally

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/api/contact`.

## 4. Deploy it

Netlify only hosts static frontends, so this backend needs a separate host.
Good free/cheap options for a Spring Boot app:

- **Render** (render.com) — easiest, free tier available, auto-deploys from GitHub
- **Railway** (railway.app) — similarly simple, usage-based free tier
- **Fly.io** — more control, still has a free allowance

Whichever you pick, set the same environment variables (`MAIL_USERNAME`,
`MAIL_PASSWORD`, `OWNER_EMAIL`, `ALLOWED_ORIGINS`) in that platform's
dashboard — never commit them to Git.

Once deployed, update `ALLOWED_ORIGINS` to include your live Netlify URL,
and point the Angular frontend's `apiUrl` (see `environment.prod.ts`) at
your new backend's URL instead of `localhost:8080`.

## Notes

- The honeypot field (`website`) must stay in the Angular form exactly as
  it is now — hidden, `tabindex="-1"`, never shown to real users. Bots that
  auto-fill every field will fill it in, and the backend silently drops
  those submissions while still returning a "success" response (so bots
  don't learn to look for another way in).
- Validation messages (name too short, invalid email, etc.) are returned
  in `message` with a 400 status, so your Angular error handling can just
  display `err.error.message` directly.
