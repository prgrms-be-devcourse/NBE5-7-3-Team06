import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../../api/axiosInstance';

const VacationEdit = () => {
    const navigate = useNavigate();
    const { requestId } = useParams();

    const [vacationTypes, setVacationTypes] = useState([]);
    const [vacationInfo, setVacationInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [originalVacation, setOriginalVacation] = useState(null);

    // 초기 상태
    const [form, setForm] = useState({
        from: '',
        to: '',
        halfDayType: 'AM', // AM: 오전반차(9-13시), PM: 오후반차(14-18시)
        reason: '',
        vacationType: ''
    });

    const [errors, setErrors] = useState({});

    // 데이터 로드
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // 휴가 유형 가져오기
                const typesResponse = await api.get('/codes/group/VACATION_TYPE');
                setVacationTypes(typesResponse.data);

                // 연차 정보 가져오기
                const infoResponse = await api.get('/vacations/my');
                setVacationInfo(infoResponse.data);

                // 기존 휴가 내역 가져오기
                const historyResponse = await api.get('/vacations', { params: { page: 0 } });
                const vacationData = historyResponse.data.content.find(item => item.requestId === parseInt(requestId, 10));

                if (vacationData) {
                    setOriginalVacation(vacationData);

                    // 날짜 포맷 변환 (YYYY-MM-DD) - 시간대 문제 해결
                    const formatDate = (dateString) => {
                        if (!dateString) return '';

                        // 날짜 문자열이 있으면 시간대 오차를 조정하기 위해 날짜만 추출
                        const date = new Date(dateString);
                        const year = date.getFullYear();
                        const month = String(date.getMonth() + 1).padStart(2, '0');
                        const day = String(date.getDate()).padStart(2, '0');

                        return `${year}-${month}-${day}`;
                    };

                    // 휴가 타입 코드 찾기
                    const typeCode = typesResponse.data.find(t => t.name === vacationData.vacationType)?.code || '';

                    // 반차인지 확인하고 오전/오후 판단
                    let halfDayType = 'AM';
                    if (typeCode === '05') {
                        const fromDate = new Date(vacationData.from);
                        const fromHours = fromDate.getHours();
                        halfDayType = (fromHours < 12) ? 'AM' : 'PM';
                    }

                    setForm({
                        from: formatDate(vacationData.from),
                        to: formatDate(vacationData.to),
                        halfDayType: halfDayType,
                        reason: vacationData.reason,
                        vacationType: typeCode
                    });
                } else {
                    alert('휴가 정보를 찾을 수 없습니다.');
                    navigate('/vacations/history');
                }
            } catch (err) {
                console.error('데이터 로드 실패:', err);
                alert('휴가 정보를 불러오는데 실패했습니다.');
                navigate('/vacations/history');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [requestId, navigate]);

    // 입력 변경 처리
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({
            ...prev,
            [name]: value
        }));

        // 에러 메시지 지우기
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    // 폼 검증
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

    // 휴가 수정 제출
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            setSubmitting(true);

            // 날짜와 시간 합치기 (UTC 기준으로 설정)
            let fromDateTime, toDateTime;

            const [fromYear, fromMonth, fromDay] = form.from.split('-').map(Number);

            if (form.vacationType === '05') { // 반차인 경우
                if (form.halfDayType === 'AM') { // 오전반차
                    fromDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 9));
                    toDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 13));
                } else { // 오후반차
                    fromDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 14));
                    toDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 18));
                }
            } else { // 다른 휴가 유형
                const [toYear, toMonth, toDay] = form.to.split('-').map(Number);
                fromDateTime = new Date(Date.UTC(fromYear, fromMonth - 1, fromDay, 9));
                toDateTime = new Date(Date.UTC(toYear, toMonth - 1, toDay, 18));
            }

            const requestData = {
                from: fromDateTime.toISOString(),
                to: toDateTime.toISOString(),
                reason: form.reason,
                vacationType: form.vacationType
            };

            // 수정 요청
            await api.put(`/vacations/${requestId}`, requestData);
            alert('휴가 수정이 완료되었습니다.');
            navigate('/vacations/history'); // 휴가 내역 페이지로 이동
        } catch (err) {
            console.error('휴가 수정 실패:', err);

            if (err.response?.data) {
                const errorMessage = err.response.data.message || '휴가 신청 중 오류가 발생했습니다.';
                alert(errorMessage);

                // 폼 에러 상태 업데이트 (필요한 경우)
                if (err.response.data.errors) {
                    setErrors(err.response.data.errors);
                }
            } else {
                alert('휴가 수정 중 오류가 발생했습니다.');
            }
        } finally {
            setSubmitting(false);
        }
    };

    // 취소 처리
    const handleCancel = () => {
        navigate('/vacations/history');
    };

    // 날짜 차이 계산 (일수)
    const calculateDays = () => {
        if (!form.from) return 0;

        // 반차의 경우 0.5일 반환
        if (form.vacationType === '05') {
            return 0.5;
        }

        if (!form.to) return 0;

        const fromDate = new Date(form.from);
        const toDate = new Date(form.to);

        const diffTime = Math.abs(toDate - fromDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

        return diffDays;
    };

    // 휴가 사용일수 변경 계산
    const calculateUsedDays = () => {
        if (originalVacation) {
            // 기존 사용일과 새로운 사용일 계산
            const originalDays = originalVacation.vacationType.includes('반차') ? 0.5 :
                Math.ceil(Math.abs(new Date(originalVacation.to) - new Date(originalVacation.from)) / (1000 * 60 * 60 * 24)) + 1;

            const currentDays = calculateDays();
            return currentDays - originalDays;
        }

        return 0;
    };

    const vacationDays = calculateDays();
    const usedDays = calculateUsedDays();

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 p-6">
                <div className="max-w-3xl mx-auto">
                    <div className="flex flex-col items-center justify-center h-64">
                        <div className="w-12 h-12 rounded-full border-2 border-t-blue-500 border-b-blue-500 border-l-gray-200 border-r-gray-200 animate-spin"></div>
                        <p className="mt-4 text-gray-600 font-medium">휴가 정보를 불러오는 중입니다...</p>
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
                    <h1 className="text-2xl font-bold text-gray-800">휴가 수정</h1>
                    <p className="text-gray-600">필요한 정보를 수정하여 휴가를 업데이트해주세요.</p>
                </div>

                {/* 휴가 수정 폼 */}
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
                                disabled={submitting ||
                                    (vacationInfo &&
                                        vacationInfo.remainCount < usedDays &&
                                        form.vacationType !== '05' &&
                                        usedDays > 0)}
                                className="px-4 py-2 bg-blue-600 border border-transparent rounded-md shadow-sm text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {submitting ? '처리 중...' : '휴가 수정'}
                            </button>
                        </div>
                    </div>
                </form>

                {/* 유의사항 안내 카드 */}
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-4">
                    <h3 className="text-sm font-medium text-blue-800 mb-2">유의사항</h3>
                    <ul className="text-sm text-blue-700 space-y-1 pl-5 list-disc">
                        <li>연차 휴가는 근로기준법에 따라 사용 가능합니다.</li>
                        <li>휴가 수정은 결재 대기 상태에서만 가능합니다.</li>
                        <li>기존에 승인된 휴가는 수정이 불가능합니다.</li>
                        <li>반차는 오전반차(09:00-13:00), 오후반차(14:00-18:00)로 구분됩니다.</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default VacationEdit;