async function sha256(str) {
  const buf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(str));
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

function esc(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

export default {
  async fetch(req, env) {
    const url = new URL(req.url);

    if (url.pathname === "/api/feedback") {
      if (req.method === "OPTIONS") {
        return new Response(null, {
          status: 204,
          headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "POST, OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type",
          },
        });
      }

      if (req.method !== "POST") {
        return Response.json({ error: "method not allowed" }, { status: 405 });
      }

      let body;
      try {
        body = await req.json();
      } catch {
        return Response.json({ error: "bad json" }, { status: 400 });
      }

      const msg = (body?.message || "").trim();
      if (msg.length < 2 || msg.length > 4000) {
        return Response.json({ error: "message must be between 2 and 4000 chars" }, { status: 400 });
      }

      const rawReply = (body?.email || "").trim().slice(0, 160);
      const validEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(rawReply) ? rawReply.toLowerCase() : "";
      const page = (body?.page || "overview").trim().slice(0, 80);

      const toAddr = "hello@fy2ne.me";
      const fromAddr = validEmail || "anon@muto.fy2ne.me";
      const subject = `[Muto Docs] Feedback (${page})${validEmail ? ` from ${validEmail}` : ""}`;
      const text = [
        `Page: ${page}`,
        `Reply-To: ${validEmail || "not provided"}`,
        "",
        msg,
      ].join("\n");

      const html = `<div style="font-family:monospace;font-size:13px;line-height:1.6;color:#e6edf3;background:#0a0a0a;padding:16px;border-radius:8px;border:1px solid #222">
<div style="color:#888;margin-bottom:12px">
  <div><strong>Page:</strong> ${esc(page)}</div>
  <div><strong>Reply-To:</strong> ${validEmail ? `<a href="mailto:${esc(validEmail)}" style="color:#f59e0b">${esc(validEmail)}</a>` : "not provided"}</div>
</div>
<pre style="white-space:pre-wrap;font-family:inherit;margin:0;color:#fff">${esc(msg)}</pre>
</div>`;

      if (env.DB) {
        try {
          let ident = await env.DB.prepare(
            "SELECT id, workspace_id FROM identities WHERE LOWER(address) = ? LIMIT 1"
          )
            .bind(toAddr)
            .first();

          let wsId = ident?.workspace_id;
          let identId = ident?.id;

          if (!ident) {
            let ws = await env.DB.prepare(
              "SELECT id FROM workspaces WHERE LOWER(domain) = 'fy2ne.me' LIMIT 1"
            ).first();
            if (!ws) {
              ws = await env.DB.prepare(
                "SELECT id FROM workspaces ORDER BY created_at ASC LIMIT 1"
              ).first();
            }
            if (ws) {
              wsId = ws.id;
              identId = `id_${crypto.randomUUID().replace(/-/g, "").slice(0, 16)}`;
              await env.DB.prepare(
                "INSERT INTO identities (id, workspace_id, address, display_name, active, created_at) VALUES (?, ?, ?, 'hello', 1, ?)"
              )
                .bind(identId, wsId, toAddr, Date.now())
                .run();
            }
          }

          if (wsId) {
            const cleanSub = subject.replace(/^(re|fwd|fw|aw|sv):\s*/i, "").trim().toLowerCase();
            const threadKey = `${cleanSub}::${[fromAddr, toAddr].sort().join(",")}`;
            const threadId = `th_${(await sha256(threadKey)).slice(0, 16)}`;
            const emailId = `em_${crypto.randomUUID().replace(/-/g, "").slice(0, 16)}`;

            await env.DB.prepare(
              `INSERT INTO emails (
                id, workspace_id, identity_id, direction, from_address, to_address,
                subject, body_text, body_html, raw_headers, read, starred, labels,
                thread_id, created_at
              ) VALUES (?, ?, ?, 'in', ?, ?, ?, ?, ?, ?, 0, 0, '["muto"]', ?, ?)`
            )
              .bind(
                emailId,
                wsId,
                identId || null,
                fromAddr,
                toAddr,
                subject,
                text,
                html,
                JSON.stringify({ source: "muto-docs", page, reply_to: validEmail || null }),
                threadId,
                Date.now()
              )
              .run();

            return Response.json({ ok: true, id: emailId });
          }
        } catch (e) {
          // fall through to inbound webhook
        }
      }

      const upstream = await fetch("https://emails.fy2ne.me/api/inbound", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          to: toAddr,
          from: fromAddr,
          subject,
          text,
          html,
        }),
      });

      if (!upstream.ok) {
        return Response.json({ error: "delivery failed" }, { status: 502 });
      }

      return Response.json({ ok: true });
    }

    return env.ASSETS.fetch(req);
  },
};
