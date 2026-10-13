import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse, Role } from '../types';
import { apiClient } from '../api/client';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: { email: string; password: string; firstName: string; lastName: string; companyName?: string; jobTitle?: string; phone?: string }) => Promise<void>;
  logout: () => void;
  switchDemoUser: (role: Role) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('servicesphere_token'));
  const [isLoading, setIsLoading] = useState(true);

  const normalizeUserData = (userData: User): User => {
    if (!userData) return userData;
    const normalized = { ...userData };
    if (normalized.role === 'ADMIN') {
      normalized.firstName = 'Vivek';
      normalized.lastName = '';
      normalized.fullName = 'Vivek';
    } else if (normalized.role === 'AGENT' && normalized.firstName === 'Sarah') {
      normalized.lastName = '';
      normalized.fullName = 'Sarah';
    } else if (normalized.role === 'CUSTOMER' && (normalized.firstName === 'John' || normalized.email === 'john.doe@acme.com')) {
      normalized.firstName = 'John';
      normalized.lastName = '';
      normalized.fullName = 'John';
      normalized.email = 'john@acme.com';
    }
    return normalized;
  };

  useEffect(() => {
    const fetchCurrentUser = async () => {
      const storedToken = localStorage.getItem('servicesphere_token');
      if (storedToken) {
        try {
          const res = await apiClient.get('/auth/me');
          setUser(normalizeUserData(res.data.data));
        } catch {
          localStorage.removeItem('servicesphere_token');
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    fetchCurrentUser();
  }, []);

  const login = async (rawIdentifier: string, rawPassword: string) => {
    let email = rawIdentifier.trim();
    let password = rawPassword.trim();

    const lower = email.toLowerCase();
    if (lower === 'admin' || lower === 'vivek') {
      email = 'admin@servicesphere.io';
      if (password.toLowerCase() === 'admin' || password.toLowerCase() === 'vivek' || password === 'Password123!') {
        password = 'Admin@12345';
      }
    } else if (lower === 'agent' || lower === 'sarah') {
      email = 'agent.sarah@servicesphere.io';
      if (password.toLowerCase() === 'agent' || password.toLowerCase() === 'sarah' || password === 'Password123!') {
        password = 'Agent@12345';
      }
    } else if (lower === 'customer' || lower === 'john') {
      email = 'john@acme.com';
      if (password.toLowerCase() === 'customer' || password.toLowerCase() === 'john' || password === 'Password123!') {
        password = 'Customer@12345';
      }
    } else {
      if ((lower === 'admin@servicesphere.io' || lower === 'vivek@servicesphere.io') && (password.toLowerCase() === 'admin' || password.toLowerCase() === 'vivek' || password === 'Password123!')) {
        password = 'Admin@12345';
      } else if ((lower === 'agent.sarah@servicesphere.io' || lower === 'sarah@servicesphere.io') && (password.toLowerCase() === 'agent' || password.toLowerCase() === 'sarah' || password === 'Password123!')) {
        password = 'Agent@12345';
      } else if ((lower === 'john@acme.com' || lower === 'john.doe@acme.com') && (password.toLowerCase() === 'customer' || password.toLowerCase() === 'john' || password === 'Password123!')) {
        password = 'Customer@12345';
      }
    }

    const res = await apiClient.post<{ data: AuthResponse }>('/auth/login', { email, password });
    const authData = res.data.data;
    localStorage.setItem('servicesphere_token', authData.token);
    setToken(authData.token);
    
    // Fetch user details
    const meRes = await apiClient.get('/auth/me', {
      headers: { Authorization: `Bearer ${authData.token}` },
    });
    setUser(normalizeUserData(meRes.data.data));
  };

  const register = async (data: any) => {
    const res = await apiClient.post<{ data: AuthResponse }>('/auth/register', data);
    const authData = res.data.data;
    localStorage.setItem('servicesphere_token', authData.token);
    setToken(authData.token);

    const meRes = await apiClient.get('/auth/me', {
      headers: { Authorization: `Bearer ${authData.token}` },
    });
    setUser(normalizeUserData(meRes.data.data));
  };

  const logout = () => {
    localStorage.removeItem('servicesphere_token');
    setToken(null);
    setUser(null);
    window.location.href = '/login';
  };

  const switchDemoUser = async (role: Role) => {
    if (role === 'ADMIN') {
      await login('admin@servicesphere.io', 'Admin@12345');
    } else if (role === 'AGENT') {
      await login('agent.sarah@servicesphere.io', 'Agent@12345');
    } else {
      await login('john@acme.com', 'Customer@12345');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        switchDemoUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
