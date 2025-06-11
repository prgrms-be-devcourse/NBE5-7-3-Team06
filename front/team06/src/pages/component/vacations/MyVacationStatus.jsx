import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/axiosInstance';

const MyVacationStatus = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [vacationInfo, setVacationInfo] = useState(null);
    const [error, setError] = useState(null);

    // 연차 정보 가져오기
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                // 연차 정보 가져오기 (/vacations/my)
                const infoResponse = await api.get('/vacations/my');
                setVacationInfo(infoResponse.data);
            } catch (err) {
                console.error('데이터 로드 실패:', err);
                setError('데이터를 불러오는 중 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    // 휴가 신청 페이지로 이동
    const handleRequestVacation = () => {
        navigate('/vacations/request');
    };

    // 휴가 내역 상세 페이지로 이동
    const handleViewHistory = () => {
        navigate('/vacations/history');
    };

    // 로딩 중 UI
    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 p-6">
                <div className="max-w-5xl mx-auto">
                    <div className="flex flex-col items-center justify-center h-64">
                        <div className="w-12 h-12 rounded-full border-2 border-t-blue-500 border-b-blue-500 border-l-gray-200 border-r-gray-200 animate-spin"></div>
                        <p className="mt-4 text-gray-600 font-medium">데이터를 불러오는 중입니다...</p>
                    </div>
                </div>
            </div>
        );
    }

    // 에러 UI
    if (error) {
        return (
            <div className="min-h-screen bg-gray-50 p-6">
                <div className="max-w-5xl mx-auto">
                    <div className="bg-white rounded-lg shadow-md p-8 text-center">
                        <div className="text-red-500 text-5xl mb-4">⚠️</div>
                        <h2 className="text-xl font-bold text-gray-800 mb-2">문제가 발생했습니다</h2>
                        <p className="text-gray-600 mb-6">{error}</p>
                        <button
                            onClick={() => window.location.reload()}
                            className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors"
                        >
                            다시 시도
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-5xl mx-auto">
                {/* 헤더 */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">개인 연차 현황</h1>
                    <p className="text-gray-600">현재 보유한 연차 현황을 확인할 수 있습니다.</p>
                </div>

                {/* 연차 정보 카드 */}
                <div className="bg-white rounded-lg shadow-md mb-8 overflow-hidden">
                    <div className="p-6 border-b border-gray-200">
                        <h2 className="text-lg font-semibold text-gray-800 mb-4">내 연차 정보</h2>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div
                                className="bg-blue-50 rounded-lg p-6 flex flex-col items-center justify-center border border-blue-100 h-36">
                                <span className="text-sm text-gray-600 mb-2">부여 휴가</span>
                                <span className="text-3xl font-bold text-blue-600">{vacationInfo?.totalCount}일</span>
                            </div>

                            <div
                                className="bg-indigo-50 rounded-lg p-6 flex flex-col items-center justify-center border border-indigo-100 h-36">
                                <span className="text-sm text-gray-600 mb-2">사용 휴가</span>
                                <span className="text-3xl font-bold text-indigo-600">{vacationInfo?.useCount}일</span>
                            </div>

                            <div
                                className="bg-emerald-50 rounded-lg p-6 flex flex-col items-center justify-center border border-emerald-100 h-36">
                                <span className="text-sm text-gray-600 mb-2">잔여 연차</span>
                                <span
                                    className="text-3xl font-bold text-emerald-600">{vacationInfo?.remainCount}일</span>
                            </div>
                        </div>
                    </div>

                    {/* 연차 사용 진행 상태바 */}
                    <div className="px-6 py-4 bg-gray-50">
                        <div className="flex justify-between mb-2">
                            <span className="text-sm font-medium text-gray-700">연차 사용률</span>
                            <span className="text-sm font-medium text-gray-700">
                                {vacationInfo?.useCount} / {vacationInfo?.totalCount}일
                                ({vacationInfo ? Math.round((vacationInfo.useCount / vacationInfo.totalCount) * 100) : 0}%)
                            </span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2.5">
                            <div
                                className="bg-blue-600 h-2.5 rounded-full"
                                style={{width: `${vacationInfo ? (vacationInfo.useCount / vacationInfo.totalCount) * 100 : 0}%`}}
                            ></div>
                        </div>
                    </div>
                </div>

                {/* 액션 카드 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                    <div className="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg shadow-md p-6 text-white">
                        <div className="flex flex-col h-full justify-between">
                            <div>
                                <h3 className="font-bold text-xl mb-2">휴가 신청하기</h3>
                                <p className="opacity-90 mb-4">휴가 신청서를 작성하고 승인 요청을 진행하세요.</p>
                            </div>
                            <button
                                onClick={handleRequestVacation}
                                className="mt-4 bg-white text-indigo-700 font-medium px-4 py-2 rounded-md self-start hover:bg-opacity-90 transition-all"
                            >
                                휴가 신청 &rarr;
                            </button>
                        </div>
                    </div>

                    <div className="bg-gradient-to-br from-emerald-500 to-teal-600 rounded-lg shadow-md p-6 text-white">
                        <div className="flex flex-col h-full justify-between">
                            <div>
                                <h3 className="font-bold text-xl mb-2">휴가 내역 확인</h3>
                                <p className="opacity-90 mb-4">지금까지 신청한 모든 휴가 내역을 확인하세요.</p>
                            </div>
                            <button
                                onClick={handleViewHistory}
                                className="mt-4 bg-white text-emerald-700 font-medium px-4 py-2 rounded-md self-start hover:bg-opacity-90 transition-all"
                            >
                                내역 확인 &rarr;
                            </button>
                        </div>
                    </div>
                </div>

                {/* 안내 카드 */}
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-5">
                    <h3 className="text-sm font-medium text-blue-800 mb-3 flex items-center">
                        <span className="mr-2">ℹ️</span> 연차 사용 안내
                    </h3>
                    <ul className="text-sm text-blue-700 space-y-2 pl-5 list-disc">
                        <li>연차는 1년 단위로 부여되며, 미사용 시 소멸됩니다.</li>
                        <li>휴가 신청은 시작일 기준 3일 전까지 신청해야 합니다.</li>
                        <li>반차는 오전반차(09:00-13:00), 오후반차(14:00-18:00)로 구분됩니다.</li>
                        <li>결재 상태가 대기 중인 경우에만 수정 및 취소가 가능합니다.</li>
                        <li>휴가와 관련하여 문의사항이 있으시면 인사팀으로 연락주세요.</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default MyVacationStatus;