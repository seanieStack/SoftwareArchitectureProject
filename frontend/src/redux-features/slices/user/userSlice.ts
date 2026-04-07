import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import type { LoginFormValues } from '../../../validation-schemas/loginSchema'
import {
  loginRequest,
  registerRequest,
  type RegisterRequestBody,
} from './userService'

export type AuthAsyncStatus = 'idle' | 'loading' | 'succeeded' | 'failed'

export const loginUser = createAsyncThunk<
  unknown,
  Pick<LoginFormValues, 'email' | 'password'>,
  { rejectValue: string }
>('user/login', async (payload, { rejectWithValue }) => {
  try {
    return await loginRequest(payload)
  } catch (e) {
    const message = e instanceof Error ? e.message : 'Login failed'
    return rejectWithValue(message)
  }
})

export const registerUser = createAsyncThunk<
  unknown,
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

type UserState = {
  profile: unknown | null
  loginStatus: AuthAsyncStatus
  loginError: string | null
  registerStatus: AuthAsyncStatus
  registerError: string | null
}

const initialState: UserState = {
  profile: null,
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
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loginStatus = 'loading'
        state.loginError = null
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loginStatus = 'succeeded'
        state.profile = action.payload
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
        state.profile = action.payload
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.registerStatus = 'failed'
        state.registerError =
          action.payload ?? action.error.message ?? 'Registration failed'
      })
  },
})

export const { clearLoginError, clearRegisterError, resetAuthForms } =
  userSlice.actions
export const userReducer = userSlice.reducer
