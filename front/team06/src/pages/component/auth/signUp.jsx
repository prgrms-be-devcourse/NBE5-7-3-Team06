
import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { colors, spacing, typography } from '../styles/design-tokens';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/axiosInstance';


const SignUp = () => {

    const [positions, setPositions] = useState([]);
    const [depts, setDepts] = useState([]);

    useEffect(() => {
        // 직위 코드 가져오기
        api.get('/codes/group/POSITION')
            .then(res => {
                setPositions(res.data);
            })
            .catch(err => {
                console.error("직위 조회 실패", err);
            });

        // 부서 목록 가져오기
        api.get('/depts')

            .then(res => {
                setDepts(res.data);
            })
            .catch(err => {
                console.error("부서 조회 실패", err);
            });
    }, []);


    const [emailCheckMessage, setEmailCheckMessage] = useState('');
    const [isEmailAvailable, setIsEmailAvailable] = useState(null);


    const handleEmailCheck = async () => {
        if (!form.email) {
            setEmailCheckMessage('이메일을 입력해주세요.');
            setIsEmailAvailable(false);
            return;
        }



        const res = await api.get(`/auth/email-duplicate-check?email=${form.email}`);
        const isDuplicated = res.data.isEmailDuplicated;
        if(!isDuplicated){
            setEmailCheckMessage('✅ 사용 가능한 이메일입니다.');
            setIsEmailAvailable(true);
        }else{
            setEmailCheckMessage('❌ 이미 존재하는 이메일입니다.');
            setIsEmailAvailable(false);
        }
    };



    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: '',
        email: '',
        dept: '',
        position: '',
        joinDate: '',
        birth: '',
        password: '',
    });


    useEffect(() => {
        setIsEmailAvailable(false);
        setEmailCheckMessage('');
    }, [form.email]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;


    const [passwordMessage, setPasswordMessage] = useState('');

    useEffect(() => {
        if (!form.password) {
            setPasswordMessage('');
        } else if (!passwordRegex.test(form.password)) {
            setPasswordMessage('영문자, 숫자, 특수문자를 포함해 8자 이상으로 입력해주세요.');
        } else {
            setPasswordMessage('');
        }
    }, [form.password]);


    const [errorMessages , setErrorMessages] = useState({});

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!passwordRegex.test(form.password)) {
            setPasswordMessage('비밀번호 조건을 다시 확인해주세요.');
            return;
        }

        try {

            await api.post('/auth/signup', {

                ...form,
                joinDate: new Date(form.joinDate),
            });
            alert('회원가입 성공!');
            window.location.href = '/auth/login';
        } catch (err) {
            const httpsStatus = err.response?.status;
            const customStatus = err.response?.data?.status;
            const codeName = err.response?.data?.codeName;

            if(customStatus === 400 && codeName === 'BAD_REQUEST_VALIDATION'){
                setErrorMessages(err.response.data.errors);
            }
        }
    };


    const handleCancel = () => {
        navigate('/auth/login');
    };

    return (
        <FormContainer onSubmit={handleSubmit}>
            <Header>회원가입</Header>

            <InputGroup>
                <Label htmlFor="email">이메일</Label>
                <EmailCheckWrapper>
                    <EmailInput
                        name="email"
                        value={form.email}
                        onChange={handleChange}
                        required
                    />
                    <EmailCheckButton type="button" onClick={handleEmailCheck}>
                        중복 확인
                    </EmailCheckButton>
                </EmailCheckWrapper>
                {emailCheckMessage && (
                    <p style={{ color: isEmailAvailable ? 'green' : 'red' }}>
                        {emailCheckMessage}
                    </p>
                )}
                {errorMessages.email && (
                    <ErrorText>{errorMessages.email}</ErrorText>
                )}
                <Label htmlFor="name">이름</Label>
                <Input
                    name="name"
                    type="text"
                    value={form.name}
                    onChange={handleChange}
                    required
                />
                {errorMessages.name && (
                    <ErrorText>{errorMessages.name}</ErrorText>
                )}
                <label htmlFor="dept">부서</label>
                <StyledSelect
                    id="dept"
                    name="dept"
                    value={form.dept}
                    onChange={handleChange}
                    required
                >
                    <option value="">선택</option>
                    {depts.map(dept => (
                        <option key={dept.id} value={dept.id}>{dept.name}</option>
                    ))}
                </StyledSelect>
                {errorMessages.dept && (
                    <ErrorText>{errorMessages.dept}</ErrorText>
                )}

                <label htmlFor="position">직위</label>
                <StyledSelect
                    id="position"
                    name="position"
                    value={form.position}
                    onChange={handleChange}
                    required
                >
                    <option value="">선택</option>
                    {positions.map(pos => (
                        <option key={pos.code} value={pos.code}>{pos.name}</option>
                    ))}
                </StyledSelect>
                {errorMessages.position && (
                    <ErrorText>{errorMessages.position}</ErrorText>
                )}
                <Label htmlFor="joinDate">입사 날짜</Label>
                <Input name="joinDate" type="date" value={form.joinDate} onChange={handleChange} required  />
                {errorMessages.joinDate && (
                    <ErrorText>{errorMessages.joinDate}</ErrorText>
                )}

                <Label htmlFor="birth">생년월일</Label>
                <Input name="birth" type="date" value={form.birth} onChange={handleChange} required  />
                {errorMessages.birth && (
                    <ErrorText>{errorMessages.birth}</ErrorText>
                )}

                <Label htmlFor="password">비밀번호</Label>
                <Input name="password" type="password" value={form.password} onChange={handleChange} required  />
                {errorMessages.password && (
                    <ErrorText>{errorMessages.password}</ErrorText>
                )}
                {passwordMessage && (
                    <ErrorText>{passwordMessage}</ErrorText>
                )}
            </InputGroup>

            <ButtonGroup>
                <PrimaryButton disabled={!isEmailAvailable} type="submit">회원가입</PrimaryButton>
                <SecondaryButton type="button" onClick={handleCancel}>취소</SecondaryButton>
            </ButtonGroup>
        </FormContainer>
    );
};

export default SignUp;

// styled-components
const FormContainer = styled.form`
  max-width: 480px;
  margin: 0 auto;
  margin-top: 100px;
  padding: ${spacing.lg};
  background: ${colors.primaryBackground};
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
`;

const Label = styled.label`
  font-size: ${typography.fontSizes.sm};
  color: ${colors.primaryText};
  margin-bottom: ${spacing.xs};
`;

const Input = styled.input`
  height: 40px;
  padding: 0 ${spacing.md};
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

const ButtonGroup = styled.div`
  display: flex;
  justify-content: space-between;
  gap: ${spacing.sm};
  margin-top: ${spacing.lg};
`;

const PrimaryButton = styled.button`
  flex: 1;
  background-color: ${colors.primary};
  color: white;
  font-size: ${typography.fontSizes.md};
  padding: ${spacing.sm} ${spacing.lg};
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    background-color: #1565c0;
  }
&:disabled {
background-color: ${colors.borderColor}; // 흐리게
color: #ccc;
cursor: not-allowed;
opacity: 0.6;
  }
`;
const SecondaryButton = styled.button`
  flex: 1;
  background-color: transparent;
  color: ${colors.primary};
  font-size: ${typography.fontSizes.md};
  padding: ${spacing.sm} ${spacing.lg};
  border: 1px solid ${colors.primary};
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    background-color: rgba(25, 118, 210, 0.05);
  }
`;
const EmailCheckWrapper = styled.div`
  display: flex;
  gap: ${spacing.sm};
  align-items: center;
`;

const EmailCheckButton = styled.button`
  white-space: nowrap;
  padding: 0 ${spacing.md};
  height: 40px;
  font-size: ${typography.fontSizes.sm};
  border: 1px solid ${colors.primary};
  border-radius: 4px;
  background-color: transparent;
  color: ${colors.primary};
  cursor: pointer;
  font-weight: 500;

  &:hover {
    background-color: rgba(25, 118, 210, 0.05);
  }
`;

const StyledSelect = styled.select`
  height: 40px;
  padding: 0 ${spacing.md};
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
const EmailInput = styled(Input)`
  width: 100%;
  min-width: 280px;
  flex: 1;
`;
const ErrorText = styled.p`
  color: red;
  font-size: 12px;
  margin-top: 4px;
  margin-left: 2px;
`;

