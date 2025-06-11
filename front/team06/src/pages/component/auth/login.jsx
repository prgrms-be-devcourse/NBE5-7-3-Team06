
import React, { useState } from 'react';
import styled from 'styled-components';
import { colors, spacing, typography } from '../styles/design-tokens';

import { useNavigate } from 'react-router-dom';
import api from '../../../api/axiosInstance';


const Login = () => {

    const navigate = useNavigate();

    const goToSignUp = () => {
        navigate("/auth/signup");
    }

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();

        try{

            const response = await api.post("/auth/login",
                {email,password},
                {withCredentials: true});

            const {token} = response.data;

            localStorage.setItem("accessToken",token.accessToken);
            localStorage.setItem("userId",token.id);
            localStorage.setItem("userName",token.name);
            localStorage.setItem("userRole",token.role);

            alert(`${token.name}님 환영합니다.`);
            window.location.href = '/vacations/calendar';


        }catch(error){
            if(error.response){
                const {message,Codename,status} = error.response.data;
                alert(message);
            }
        }

    };

    return (
        <FormContainer onSubmit={handleLogin}>
            <Header>로그인</Header>
            <InputGroup>
                <InputWrapper>
                    <Label htmlFor="email">이메일</Label>
                    <Input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </InputWrapper>
                <InputWrapper>
                    <Label htmlFor="password">비밀번호</Label>
                    <Input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </InputWrapper>
            </InputGroup>
            <PrimaryButton type="submit">로그인</PrimaryButton>
            <SignupText>
                계정이 없으신가요? <SignupLink onClick={goToSignUp}>회원가입</SignupLink>
            </SignupText>
        </FormContainer>
    );
};

export default Login;

// styled-components
const FormContainer = styled.form`
  display: flex;
  flex-direction: column;
  max-width: 400px;
  margin: 0 auto;
  margin-top: 100px;
  background-color: ${colors.primaryBackground};
  padding: ${spacing.lg};
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
`;

const Header = styled.h2`
  font-size: ${typography.fontSizes.xxl};
  font-weight: 700;
  margin-bottom: ${spacing.lg};
  color: ${colors.primaryText};
  text-align: center;
`;

const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${spacing.md};
  margin-bottom: ${spacing.md};
`;

const InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  margin-bottom: ${spacing.xs};
  color: ${colors.primaryText};
  font-size: ${typography.fontSizes.sm};
`;

const Input = styled.input`
  width: 100%;
  height: 40px;
  font-size: ${typography.fontSizes.md};
  font-family: ${typography.fontFamily};
  color: ${colors.primaryText};
  border: 1px solid ${colors.borderColor};
  border-radius: 4px;
  background-color: #fff;

  &:focus {
    outline: none;
    border-color: ${colors.primary};
    box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.2);
  }
`;

const PrimaryButton = styled.button`
  background-color: ${colors.primary};
  color: white;
  font-size: ${typography.fontSizes.md};
  padding: ${spacing.sm} ${spacing.lg};
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  margin-top: ${spacing.md};

  &:hover {
    background-color: #1565c0;
  }
`;

const SignupText = styled.p`
  margin-top: ${spacing.md};
  font-size: ${typography.fontSizes.sm};
  color: ${colors.secondaryText};
  text-align: center;
`;

const SignupLink = styled.a`
  color: ${colors.primary};
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
`;
