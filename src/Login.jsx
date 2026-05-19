import { useState } from "react";
import axios from "axios";

export default function Login() {
  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [error, setError]       = useState("");
  const [success, setSuccess]   = useState("");
  const [loading, setLoading]   = useState(false);

  const handleLogin = async () => {
    setError("");
    setSuccess("");

    if (!email || !password) {
      setError("Please fill in all fields.");
      return;
    }

    setLoading(true);
    try {
      const res = await axios.post("/api/auth/login", { email, password });
      const data = res.data;

      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data.user));

      setSuccess(`Welcome back, ${data.user.firstName}! Redirecting…`);
      setTimeout(() => {
        window.location.href = "/store.html"; // or your React route
      }, 1200);
    } catch (err) {
      const msg = err.response?.data?.error || "Invalid email or password.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") handleLogin();
  };

  return (
    <div style={styles.body}>
      {/* Left Panel */}
      <div style={styles.left}>
        <div style={styles.leftOverlay} />
        <div style={styles.leftGrid} />
        <div style={styles.brand}>
          Shop<span style={{ color: "#4488ff" }}>Ease</span>
        </div>
        <div style={styles.leftContent}>
          <h1 style={styles.leftH1}>
            Welcome <span style={{ color: "#4488ff" }}>back</span> to ShopEase.
          </h1>
          <p style={styles.leftP}>
            Log in to your account to continue shopping, track your orders,
            and manage your profile.
          </p>
          <div style={styles.features}>
            {[
              { icon: "🚀", text: "Fast & free delivery" },
              { icon: "🔒", text: "Secure checkout" },
              { icon: "↩️", text: "Easy 30-day returns" },
            ].map((f) => (
              <div key={f.text} style={styles.feature}>
                <span style={styles.featIcon}>{f.icon}</span>
                {f.text}
              </div>
            ))}
          </div>
        </div>
        <div style={styles.tagline}>ShopEase — Shop smarter</div>
      </div>

      {/* Right Panel */}
      <div style={styles.right}>
        <div style={styles.formBox}>
          <h2 style={styles.h2}>Sign In</h2>
          <p style={styles.subtitle}>
            No account?{" "}
            <a href="/signup.html" style={styles.link}>
              Create one →
            </a>
          </p>

          {error && <div style={{ ...styles.alert, ...styles.alertError }}>{error}</div>}
          {success && <div style={{ ...styles.alert, ...styles.alertSuccess }}>{success}</div>}

          <div style={styles.field}>
            <label style={styles.label}>Email Address</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="john@example.com"
              style={styles.input}
              onFocus={(e) => {
                e.target.style.borderColor = "#1a6ef5";
                e.target.style.boxShadow = "0 0 0 3px rgba(26,110,245,0.08)";
                e.target.style.background = "#ffffff";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "#e8e8e6";
                e.target.style.boxShadow = "none";
                e.target.style.background = "#f9f9f8";
              }}
            />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="••••••••"
              style={styles.input}
              onFocus={(e) => {
                e.target.style.borderColor = "#1a6ef5";
                e.target.style.boxShadow = "0 0 0 3px rgba(26,110,245,0.08)";
                e.target.style.background = "#ffffff";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "#e8e8e6";
                e.target.style.boxShadow = "none";
                e.target.style.background = "#f9f9f8";
              }}
            />
          </div>

          <button
            onClick={handleLogin}
            disabled={loading}
            style={{
              ...styles.btn,
              opacity: loading ? 0.55 : 1,
              cursor: loading ? "not-allowed" : "pointer",
            }}
            onMouseEnter={(e) => { if (!loading) e.target.style.background = "#4488ff"; }}
            onMouseLeave={(e) => { if (!loading) e.target.style.background = "#1a6ef5"; }}
          >
            {loading ? "Signing in…" : "Sign In"}
          </button>

          <div style={styles.divider}>
            <span style={styles.dividerLine} />
            or
            <span style={styles.dividerLine} />
          </div>

          <p style={{ textAlign: "center", color: "#888886", fontSize: "0.85rem" }}>
            New here?{" "}
            <a href="/signup.html" style={styles.link}>
              Create an account
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}

const styles = {
  body: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    minHeight: "100vh",
    fontFamily: "'DM Sans', sans-serif",
    color: "#111110",
  },
  left: {
    position: "relative",
    background: "linear-gradient(155deg, #0c1829 0%, #0f2040 55%, #091530 100%)",
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between",
    padding: "3rem",
    overflow: "hidden",
  },
  leftOverlay: {
    position: "absolute",
    inset: 0,
    background:
      "radial-gradient(ellipse 70% 55% at 10% 90%, rgba(26,110,245,0.28) 0%, transparent 65%), radial-gradient(ellipse 50% 45% at 85% 15%, rgba(68,136,255,0.14) 0%, transparent 60%)",
    pointerEvents: "none",
    zIndex: 1,
  },
  leftGrid: {
    position: "absolute",
    inset: 0,
    backgroundImage:
      "linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px)",
    backgroundSize: "44px 44px",
    pointerEvents: "none",
  },
  brand: {
    fontFamily: "'Fraunces', serif",
    fontSize: "1.5rem",
    fontWeight: 700,
    color: "#ffffff",
    zIndex: 2,
  },
  leftContent: { zIndex: 2 },
  leftH1: {
    fontFamily: "'Fraunces', serif",
    fontSize: "clamp(2.2rem, 3.5vw, 3.1rem)",
    lineHeight: 1.15,
    fontWeight: 600,
    marginBottom: "1.25rem",
    color: "#ffffff",
  },
  leftP: {
    color: "rgba(255,255,255,0.5)",
    fontSize: "0.95rem",
    fontWeight: 300,
    lineHeight: 1.75,
    maxWidth: 340,
  },
  features: {
    display: "flex",
    flexDirection: "column",
    gap: "0.75rem",
    marginTop: "2rem",
  },
  feature: {
    display: "flex",
    alignItems: "center",
    gap: "0.75rem",
    color: "rgba(255,255,255,0.7)",
    fontSize: "0.88rem",
  },
  featIcon: {
    width: 32,
    height: 32,
    borderRadius: 8,
    background: "rgba(26,110,245,0.2)",
    border: "1px solid rgba(26,110,245,0.35)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "0.9rem",
    flexShrink: 0,
  },
  tagline: {
    fontSize: "0.73rem",
    letterSpacing: "0.13em",
    textTransform: "uppercase",
    color: "rgba(255,255,255,0.28)",
    zIndex: 2,
  },
  right: {
    background: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "3rem",
    borderLeft: "1px solid #e8e8e6",
  },
  formBox: {
    width: "100%",
    maxWidth: 400,
  },
  h2: {
    fontFamily: "'Fraunces', serif",
    fontSize: "2rem",
    fontWeight: 600,
    letterSpacing: "-0.02em",
    marginBottom: "0.4rem",
    color: "#111110",
  },
  subtitle: {
    color: "#888886",
    fontSize: "0.88rem",
    marginBottom: "2.25rem",
  },
  link: {
    color: "#1a6ef5",
    textDecoration: "none",
    fontWeight: 500,
  },
  alert: {
    padding: "0.72rem 1rem",
    borderRadius: 8,
    fontSize: "0.86rem",
    marginBottom: "1.1rem",
  },
  alertError: {
    background: "rgba(229,53,53,0.07)",
    border: "1px solid rgba(229,53,53,0.22)",
    color: "#e53535",
  },
  alertSuccess: {
    background: "rgba(24,160,88,0.08)",
    border: "1px solid rgba(24,160,88,0.22)",
    color: "#18a058",
  },
  field: { marginBottom: "1.1rem" },
  label: {
    display: "block",
    fontSize: "0.73rem",
    fontWeight: 600,
    letterSpacing: "0.09em",
    textTransform: "uppercase",
    color: "#888886",
    marginBottom: "0.45rem",
  },
  input: {
    width: "100%",
    background: "#f9f9f8",
    border: "1px solid #e8e8e6",
    borderRadius: 10,
    padding: "0.8rem 1rem",
    color: "#111110",
    fontFamily: "'DM Sans', sans-serif",
    fontSize: "0.92rem",
    outline: "none",
    transition: "border-color 0.15s, box-shadow 0.15s, background 0.15s",
    boxSizing: "border-box",
  },
  btn: {
    width: "100%",
    padding: "0.88rem",
    background: "#1a6ef5",
    color: "#ffffff",
    fontFamily: "'DM Sans', sans-serif",
    fontSize: "0.93rem",
    fontWeight: 600,
    border: "none",
    borderRadius: 10,
    letterSpacing: "0.02em",
    transition: "background 0.15s",
    marginTop: "0.4rem",
  },
  divider: {
    display: "flex",
    alignItems: "center",
    gap: "0.9rem",
    margin: "1.4rem 0",
    color: "#888886",
    fontSize: "0.78rem",
  },
  dividerLine: {
    flex: 1,
    height: 1,
    background: "#e8e8e6",
  },
};
