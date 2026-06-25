export const tokenStorage = {
  getAccessToken: () => {
    if (typeof window === "undefined") return null;
    return localStorage.getItem("accessToken");
  },

  getRefreshToken: () => {
    if (typeof window === "undefined") return null;
    return localStorage.getItem("refreshToken");
  },

  getUser: <T>() => {
    if (typeof window === "undefined") return null;
    const raw = localStorage.getItem("currentUser");
    return raw ? (JSON.parse(raw) as T) : null;
  },

  setSession: <T>(session: {
    accessToken: string;
    refreshToken?: string | null;
    user?: T | null;
  }) => {
    localStorage.setItem("accessToken", session.accessToken);

    if (session.refreshToken) {
      localStorage.setItem("refreshToken", session.refreshToken);
    }

    if (session.user) {
      localStorage.setItem("currentUser", JSON.stringify(session.user));
    }
  },

  setUser: <T>(user: T) => {
    localStorage.setItem("currentUser", JSON.stringify(user));
  },

  clearSession: () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("currentUser");
  },
};
