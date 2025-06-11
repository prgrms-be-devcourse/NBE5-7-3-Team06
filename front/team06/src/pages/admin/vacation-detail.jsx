import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import api from "../../api/axiosInstance";

export default function VacationDetail() {
    const {id: vacationId} = useParams(); // URL 파라미터에서 id 추출
    const navigate = useNavigate();

    const [vacation, setVacation] = useState(null);
    const [loading, setLoading] = useState(true);

    // 백엔드에서 휴가 상세 정보 가져오기
    useEffect(() => {
        const fetchVacationDetail = async () => {
            setLoading(true);
            try {
                const userRole = localStorage.getItem('userRole');
                let response;
                if(userRole === 'USER'){
                    response = await api.get(`/members/vacation-request/${vacationId}`);
                }else{
                    response = await api.get(`/admin/vacation-request/${vacationId}`);
                }
                // const response = await fetch(`http://localhost:8080/admin/vacation-request/${vacationId}`);

                console.log(response);
                const data = response.data;

                // if (!response.ok) {
                //   throw new Error('휴가 정보를 불러오는데 실패했습니다.');
                // }

                // const data = await response.json();
                setVacation(data);
            } catch (error) {
                console.error('Error fetching vacation detail:', error);
                alert('휴가 정보를 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        if (vacationId) {
            fetchVacationDetail();
        }
    }, [vacationId]);

    // 상태별 뱃지 스타일
    const getStatusBadge = (status) => {
        const statusStyles = {
            'IN_PROGRESS': 'bg-yellow-100 text-yellow-800',
            'APPROVED': 'bg-green-100 text-green-800',
            'REJECTED': 'bg-red-100 text-red-800',
            'CANCELED': 'bg-gray-100 text-gray-800'
        };
        return statusStyles[status] || 'bg-gray-100 text-gray-800';
    };

    // 상태 표시명 변환
    const getStatusDisplayName = (status) => {
        const statusNames = {
            'IN_PROGRESS': '대기',
            'APPROVED': '승인',
            'REJECTED': '반려',
            'CANCELED': '취소'
        };
        return statusNames[status] || status;
    };

    // 날짜 포맷팅 함수
    const formatDateTime = (dateTimeString) => {
        const date = new Date(dateTimeString);
        return date.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // 날짜만 포맷팅 함수
    const formatDate = (dateTimeString) => {
        const date = new Date(dateTimeString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    };

    // 일수 계산 함수
    const calculateDays = (from, to, type) => {
        if (!from || !to) return 0;

        // 반차인 경우 0.5일 반환
        if (type.includes('반차')) {
            return 0.5;
        }

        // 날짜만 추출하여 비교 (시간 정보 제거)
        const fromDate = new Date(from.split('T')[0]);
        const toDate = new Date(to.split('T')[0]);

        // 날짜 차이 계산 (밀리초 단위)
        const diffTime = Math.abs(toDate - fromDate);

        // 일 단위로 변환
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        // 시작일도 포함하므로 +1
        return diffDays + 1;
    };

    // approvalStep status 뱃지 스타일
    const getApprovalStepStatusBadge = (approvalStatus) => {
        if (approvalStatus.includes('PENDING')) {
            return 'bg-blue-100 text-blue-800';
        } else if (approvalStatus.includes('WAITING')) {
            return 'bg-purple-100 text-orange-800';
        } else if (approvalStatus.includes('APPROVED')) {
            return 'bg-green-100 text-green-800';
        } else if (approvalStatus.includes('REJECTED')) {
            return 'bg-orange-100 text-orange-800';
        } else {
            return 'bg-gray-100 text-red-800'; // 기타
        }
    };
    // 휴가 종류별 뱃지 스타일
    const getVacationTypeBadge = (type) => {
        if (type.includes('연차')) {
            return 'bg-blue-100 text-blue-800';
        } else if (type.includes('포상')) {
            return 'bg-purple-100 text-purple-800';
        } else if (type.includes('공가')) {
            return 'bg-green-100 text-green-800';
        } else if (type.includes('경조사')) {
            return 'bg-orange-100 text-orange-800';
        } else {
            return 'bg-gray-100 text-gray-800'; // 기타
        }
    };

    if (loading) {
        return (
            <div className="p-6 bg-gray-50 min-h-screen">
                <div className="max-w-4xl mx-auto">
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                        <div className="animate-pulse">
                            <div className="h-6 bg-gray-200 rounded-md w-1/3 mb-4"></div>
                            <div className="space-y-3">
                                <div className="h-4 bg-gray-200 rounded-md w-1/2"></div>
                                <div className="h-4 bg-gray-200 rounded-md w-2/3"></div>
                                <div className="h-4 bg-gray-200 rounded-md w-1/4"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (!vacation) {
        return (
            <div className="p-6 bg-gray-50 min-h-screen">
                <div className="max-w-4xl mx-auto">
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
                        <p className="text-gray-500">휴가 신청 정보를 찾을 수 없습니다.</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-4xl mx-auto">
                {/* 페이지 헤더 */}
                <div className="mb-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">휴가 신청 상세</h1>
                            <p className="text-gray-600 mt-2">신청 ID: #{vacation.id}</p>
                        </div>
                        <div className="flex space-x-3">
                            <button
                                onClick={() => navigate(-1)}
                                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
                            >
                                목록으로
                            </button>
                        </div>
                    </div>
                </div>

                {/* 신청자 정보 및 기본 정보 */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                    {/* 신청자 정보 */}
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                        <h2 className="text-lg font-semibold text-gray-800 mb-4">신청자 정보</h2>
                        <div className="space-y-3">
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">이름</span>
                                <span className="text-sm font-medium text-gray-900">{vacation.name}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">부서</span>
                                <span className="text-sm text-gray-900">{vacation.deptName}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">직책</span>
                                <span className="text-sm text-gray-900">{vacation.position}</span>
                            </div>
                        </div>
                    </div>

                    {/* 휴가 기본 정보 */}
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                        <h2 className="text-lg font-semibold text-gray-800 mb-4">휴가 정보</h2>
                        <div className="space-y-3">
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">휴가 종류</span>
                                <span
                                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getVacationTypeBadge(vacation.vacationType)}`}>
                  {vacation.vacationType}
                </span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">시작일</span>
                                <span className="text-sm text-gray-900">{formatDate(vacation.from)}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">종료일</span>
                                <span className="text-sm text-gray-900">{formatDate(vacation.to)}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">총 일수</span>
                                <span
                                    className="text-sm font-medium text-gray-900">{calculateDays(vacation.from, vacation.to, vacation.vacationType)}일</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 휴가 사유 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-semibold text-gray-800 mb-4">휴가 사유</h2>
                    <p className="text-sm text-gray-700 leading-relaxed">{vacation.reason}</p>
                </div>

                {/* 승인 현황 */}
                {/* 승인 현황 */}
                {vacation.approvalStepDetailUpdateResponses && vacation.approvalStepDetailUpdateResponses.length > 0 && (
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                        <h2 className="text-lg font-semibold text-gray-800 mb-4">승인 현황</h2>
                        <div className="space-y-4">
                            {vacation.approvalStepDetailUpdateResponses.map((approver, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex items-start justify-between">
                                        <div className="flex-1">
                                            <div className="flex items-center space-x-3 mb-2">
                                <span className="text-sm font-medium text-gray-900">
                                  {index + 1}차 결재자: {approver.name}
                                </span>
                                                {/* 뱃지를 여기로 이동하여 항상 표시되도록 함 */}
                                                <span
                                                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getApprovalStepStatusBadge(approver.approvalStatus)}`}>
                                  {approver.approvalStatus}
                                </span>
                                            </div>
                                            {approver.reason && (
                                                <div className="bg-gray-50 rounded-md p-3">
                                                    <p className="text-sm text-gray-700">{approver.reason}</p>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* 현재 상태 요약 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h3 className="text-lg font-semibold text-gray-800">현재 상태</h3>
                            <p className="text-sm text-gray-600 mt-1">이 휴가 신청의 처리 상태입니다.</p>
                        </div>
                        <span
                            className={`inline-flex px-4 py-2 text-sm font-medium rounded-full ${getStatusBadge(vacation.vacationRequestStatus)}`}>
              {getStatusDisplayName(vacation.vacationRequestStatus)}
            </span>
                    </div>
                </div>
            </div>
        </div>
    );
};