import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/axiosInstance';

const VacationRequest = () => {
    const navigate = useNavigate();

    // 상태 관리
    const [vacationTypes, setVacationTypes] = useState([]);
    const [vacationInfo, setVacationInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    // 폼 상태
    const [form, setForm] = useState({
        from: '',
        to: '',
        halfDayType: 'AM', // AM: 오전반차(9-13시), PM: 오후반차(14-18시)
        reason: '',
        vacationType: ''
    });

    // 에러 상태 (백엔드 응답용)
    const [errors, setErrors] = useState({});

    // 초기 데이터 로드
    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                setLoading(true);

                // 휴가 유형 코드 가져오기
                const typesResponse = await api.get('/codes/group/VACATION_TYPE');
                setVacationTypes(typesResponse.data);

                // 연차 정보 가져오기
                const infoResponse = await api.get('/vacations/my');
                setVacationInfo(infoResponse.data);

            } catch (err) {
                console.error('데이터 로드 실패:', err);
                alert('데이터를 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
    }, []);

    // 입력 값 변경 처리
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({
            ...prev,
            [name]: value
        }));

        // 입력 값 변경 시 해당 필드의 에러 메시지 제거
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    // 폼 유효성 검증
    const validateForm = () => {
        const newErrors = {};

        if (!form.from) newErrors.from = '시작일을 선택해주세요.';
        if (!form.vacationType) newErrors.vacationType = '휴가 유형을 선택해주세요.';
        if (!form.reason) newErrors.reason = '휴가 사유를 입력해주세요.';
        if (form.reason && form.reason.length < 2) newErrors.reason = '휴가 사유는 최소 2자 이상 입력해주세요.';

        // 반차가 아닌 경우에만 종료일 검증
        if (form.vacationType !== '05') {
            if (!form.to) newErrors.to = '종료일을 선택해주세요.';

            // 날짜 검증
            if (form.from && form.to) {
                const fromDate = new Date(form.from);
                const toDate = new Date(form.to);

                if (fromDate > toDate) {
                    newErrors.to = '종료일은 시작일보다 이후 날짜여야 합니다.';
                }
            }
        }

        // 현재 날짜와 비교
        if (form.from) {
            const fromDate = new Date(form.from);
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            if (fromDate < today) {
                newErrors.from = '시작일은 오늘 이후로 선택해주세요.';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const calculateDays = () => {
        if (!form.from) return 0;

        // 반차의 경우 0.5일 반환
        if (form.vacationType === '05') {
            return 0.5;
        }

        // 시작일과 종료일이 같은 경우 1일 반환
        if (!form.to) return 0;

        // 날짜 객체 생성
        const fromDate = new Date(form.from);
        const toDate = new Date(form.to);

        // 날짜 차이 계산 (밀리초 단위)
        const diffTime = Math.abs(toDate - fromDate);

        // 일 단위로 변환
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        // 시작일도 포함하므로 +1
        return diffDays + 1;
    };

    // 휴가 신청 제출
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            setSubmitting(true);

            // 날짜와 시간 합치기
            let fromDateTime, toDateTime;

            const [fromYear, fromMonth, fromDay] = form.from.split('-').map(Number);
            const [toYear, toMonth, toDay] = form.to.split('-').map(Number);
            if (form.vacationType === '05') { // 반차인 경우
                if (form.halfDayType === 'AM') { // 오전반차
                    fromDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 9));
                    toDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 13));
                } else { // 오후반차
                    fromDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 14));
                    toDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 18));
                }
            } else { // 다른 휴가 유형
                fromDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 9));
                toDateTime = new Date(Date.UTC(toYear, toMonth - 1, toDay, 18));
            }

            console.log(fromDateTime);
            console.log(toDateTime);
            const requestData = {
                from: fromDateTime.toISOString(),
                to: toDateTime.toISOString(),
                reason: form.reason,
                vacationType: form.vacationType
            };
            console.log(requestData);

            // 휴가 신청 API 호출
            await api.post('/vacations', requestData);

            alert('휴가 신청이 완료되었습니다.');
            navigate('/vacations/history'); // 휴가 내역 페이지로 이동
        } catch (err) {
            console.error('휴가 신청 실패:', err);

            if (err.response?.data) {
                const errorMessage = err.response.data.message || '휴가 신청 중 오류가 발생했습니다.';
                alert(errorMessage);

                // 폼 에러 상태 업데이트 (필요한 경우)
                if (err.response.data.errors) {
                    setErrors(err.response.data.errors);
                }
            } else {
                alert('휴가 신청 중 오류가 발생했습니다.');
            }
        } finally {
            setSubmitting(false);
        }
    };

    // 취소 처리
    const handleCancel = () => {
        navigate('/vacations/my');
    };

    // 계산된 휴가 일수
    const vacationDays = calculateDays();

    // 로딩 화면
    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 p-6">
                <div className="max-w-3xl mx-auto">
                    <div className="flex flex-col items-center justify-center h-64">
                        <div className="w-12 h-12 rounded-full border-2 border-t-blue-500 border-b-blue-500 border-l-gray-200 border-r-gray-200 animate-spin"></div>
                        <p className="mt-4 text-gray-600 font-medium">데이터를 불러오는 중입니다...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-3xl mx-auto">
                {/* 헤더 */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">휴가 신청</h1>
                    <p className="text-gray-600">필요한 정보를 입력하여 휴가를 신청해주세요.</p>
                </div>

                {/* 휴가 신청 폼 */}
                <form onSubmit={handleSubmit}>
                    <div className="bg-white rounded-lg shadow-md overflow-hidden mb-6">
                        {/* 휴가 정보 섹션 */}
                        <div className="p-6 border-b border-gray-200">
                            <h2 className="text-lg font-semibold text-gray-800 mb-4">휴가 정보</h2>

                            {/* 휴가 유형 */}
                            <div className="mb-4">
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    휴가 유형<span className="text-red-500">*</span>
                                </label>
                                <select
                                    id="vacationType"
                                    name="vacationType"
                                    value={form.vacationType}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border ${errors.vacationType ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
                                >
                                    <option value="">선택해주세요</option>
                                    {vacationTypes.map(type => (
                                        <option key={type.code} value={type.code}>
                                            {type.name}
                                        </option>
                                    ))}
                                </select>
                                {errors.vacationType && <p className="mt-1 text-sm text-red-600">{errors.vacationType}</p>}
                            </div>

                            {/* 시작일 */}
                            <div className="mb-4">
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    시작일<span className="text-red-500">*</span>
                                </label>
                                <input
                                    type="date"
                                    id="from"
                                    name="from"
                                    value={form.from}
                                    onChange={handleChange}
                                    className={`w-full px-3 py-2 border ${errors.from ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
                                />
                                {errors.from && <p className="mt-1 text-sm text-red-600">{errors.from}</p>}
                            </div>

                            {/* 종료일 - 반차가 아닌 경우에만 표시 */}
                            {form.vacationType !== '05' && (
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        종료일<span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="date"
                                        id="to"
                                        name="to"
                                        value={form.to}
                                        onChange={handleChange}
                                        className={`w-full px-3 py-2 border ${errors.to ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
                                    />
                                    {errors.to && <p className="mt-1 text-sm text-red-600">{errors.to}</p>}
                                </div>
                            )}

                            {/* 반차 선택 시 오전/오후 선택 */}
                            {form.vacationType === '05' && (
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        반차 유형<span className="text-red-500">*</span>
                                    </label>
                                    <div className="flex gap-4">
                                        <label className="inline-flex items-center">
                                            <input
                                                type="radio"
                                                name="halfDayType"
                                                value="AM"
                                                checked={form.halfDayType === 'AM'}
                                                onChange={handleChange}
                                                className="h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                                            />
                                            <span className="ml-2 text-sm text-gray-700">오전반차 (09:00-13:00)</span>
                                        </label>
                                        <label className="inline-flex items-center">
                                            <input
                                                type="radio"
                                                name="halfDayType"
                                                value="PM"
                                                checked={form.halfDayType === 'PM'}
                                                onChange={handleChange}
                                                className="h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                                            />
                                            <span className="ml-2 text-sm text-gray-700">오후반차 (14:00-18:00)</span>
                                        </label>
                                    </div>
                                </div>
                            )}

                            {/* 휴가 사유 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    휴가 사유<span className="text-red-500">*</span>
                                </label>
                                <textarea
                                    id="reason"
                                    name="reason"
                                    value={form.reason}
                                    onChange={handleChange}
                                    rows="4"
                                    placeholder="휴가 사유를 입력해주세요."
                                    className={`w-full px-3 py-2 border ${errors.reason ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
                                />
                                {errors.reason && <p className="mt-1 text-sm text-red-600">{errors.reason}</p>}
                            </div>
                        </div>

                        {/* 버튼 영역 */}
                        <div className="p-6 flex justify-end gap-3">
                            <button
                                type="button"
                                onClick={handleCancel}
                                className="px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                disabled={submitting}
                                className="px-4 py-2 bg-blue-600 border border-transparent rounded-md shadow-sm text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {submitting ? '신청 중...' : '휴가 신청'}
                            </button>
                        </div>
                    </div>
                </form>

                {/* 유의사항 안내 카드 */}
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-4">
                    <h3 className="text-sm font-medium text-blue-800 mb-2">유의사항</h3>
                    <ul className="text-sm text-blue-700 space-y-1 pl-5 list-disc">
                        <li>연차 휴가는 근로기준법에 따라 사용 가능합니다.</li>
                        <li>휴가 신청은 결재 승인 후 확정됩니다.</li>
                        <li>시작일과 종료일은 정확하게 입력해주세요.</li>
                        <li>긴급한 경우에는 팀장에게 별도로 연락 바랍니다.</li>
                        <li>반차는 오전반차(AM), 오후반차(PM)으로 구분됩니다.</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default VacationRequest;