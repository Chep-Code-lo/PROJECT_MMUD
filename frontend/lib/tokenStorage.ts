export const tokenStorage = {
  getToken: () => {
    if (typeof window === "undefined") return null;
    return localStorage.getItem("accessToken");
  },

  setToken: (token: string) => {
    localStorage.setItem("accessToken", token);
  },

  removeToken: () => {
    localStorage.removeItem("accessToken");
  },
};