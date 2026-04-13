import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import type { LoginFormValues } from '../../../validation-schemas/loginSchema'
import {
  loginRequest,
  refreshRequest,
  registerRequest,
  type RegisterRequestBody,
} from './userService'
import type { AuthResponse } from './authTypes'
import { clearPersistedAuth, loadPersistedAuth, persistAccessToken, persistAuthSession } from './persistAuth'

export type AuthAsyncStatus = 'idle' | 'loading' | 'succeeded' | 'failed'

type UserState = {
  accessToken: string | null
  refreshToken: string | null
  profile: AuthResponse['user'] | null
  loginStatus: AuthAsyncStatus
  loginError: string | null
  registerStatus: AuthAsyncStatus
  registerError: string | null
}

const persisted = loadPersistedAuth()

export type LoginThunkInput = Pick<LoginFormValues, 'email' | 'password'> & {
  rememberMe: boolean
}

export const loginUser = createAsyncThunk<
  AuthResponse,
  LoginThunkInput,
  { rejectValue: string }
>('user/login', async (payload, { rejectWithValue }) => {
  try {
    return await loginRequest({
      email: payload.email,
      password: payload.password,
    })
  } catch (e) {
    const message = e instanceof Error ? e.message : 'Login failed'
    return rejectWithValue(message)
  }
})

export const registerUser = createAsyncThunk<
  AuthResponse,
  RegisterRequestBody,
  { rejectValue: string }
>('user/register', async (payload, { rejectWithValue }) => {
  try {
    return await registerRequest(payload)
  } catch (e) {
    const message = e instanceof Error ? e.message : 'Registration failed'
    return rejectWithValue(message)
  }
})

export const refreshAccessToken = createAsyncThunk<
  AuthResponse,
  void,
  { state: { user: UserState }; rejectValue: string }
>('user/refresh', async (_, { getState, rejectWithValue }) => {
  const token = getState().user.refreshToken
  if (!token) return rejectWithValue('No refresh token')
  try {
    return await refreshRequest(token)
  } catch (e) {
    const message = e instanceof Error ? e.message : 'Token refresh failed'
    return rejectWithValue(message)
  }
})

const initialState: UserState = {
  accessToken: persisted.accessToken,
  refreshToken: persisted.refreshToken,
  profile: persisted.profile,
  loginStatus: 'idle',
  loginError: null,
  registerStatus: 'idle',
  registerError: null,
}

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    clearLoginError(state) {
      state.loginError = null
      if (state.loginStatus === 'failed') state.loginStatus = 'idle'
    },
    clearRegisterError(state) {
      state.registerError = null
      if (state.registerStatus === 'failed') state.registerStatus = 'idle'
    },
    resetAuthForms(state) {
      state.loginError = null
      state.registerError = null
      state.loginStatus = 'idle'
      state.registerStatus = 'idle'
    },
    logout(state) {
      clearPersistedAuth()
      state.accessToken = null
      state.refreshToken = null
      state.profile = null
      state.loginError = null
      state.registerError = null
      state.loginStatus = 'idle'
      state.registerStatus = 'idle'
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loginStatus = 'loading'
        state.loginError = null
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loginStatus = 'succeeded'
        state.accessToken = action.payload.accessToken
        state.refreshToken = action.payload.refreshToken ?? null
        state.profile = action.payload.user
        persistAuthSession(action.payload, action.meta.arg.rememberMe)
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loginStatus = 'failed'
        state.loginError = action.payload ?? action.error.message ?? 'Login failed'
      })
      .addCase(registerUser.pending, (state) => {
        state.registerStatus = 'loading'
        state.registerError = null
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.registerStatus = 'succeeded'
        state.accessToken = action.payload.accessToken
        state.refreshToken = action.payload.refreshToken ?? null
        state.profile = action.payload.user
        persistAuthSession(action.payload, false)
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.registerStatus = 'failed'
        state.registerError =
          action.payload ?? action.error.message ?? 'Registration failed'
      })
      .addCase(refreshAccessToken.fulfilled, (state, action) => {
        state.accessToken = action.payload.accessToken
        state.refreshToken = action.payload.refreshToken ?? null
        if (action.payload.refreshToken) {
          persistAccessToken(action.payload.accessToken, action.payload.refreshToken)
        }
      })
      .addCase(refreshAccessToken.rejected, (state) => {
        // Refresh failed — the session is unrecoverable, force logout
        clearPersistedAuth()
        state.accessToken = null
        state.refreshToken = null
        state.profile = null
      })
  },
})

export const { clearLoginError, clearRegisterError, resetAuthForms, logout } =
  userSlice.actions
export const userReducer = userSlice.reducer
