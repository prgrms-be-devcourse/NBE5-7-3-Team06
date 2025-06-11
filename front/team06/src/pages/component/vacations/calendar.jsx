import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import { useEffect,useState, useRef } from "react";
import api from '../../../api/axiosInstance';

const Calendar = () => {
    const [events, setEvents] = useState([]);

    const lastFetchedMonth = useRef(""); // ✅ 현재 요청한 월을 기억

    const [depts, setDepts] = useState([1]);
    const [selectedDeptId, setSelectedDeptId] = useState(1);

    const [user, setUser] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        api
            .get("/depts")
            .then((res) => {
                setDepts(res.data);
            })
            .catch((err) => {
                console.error("부서 조회 실패", err);
                setError("부서 정보를 불러오는 데 실패했습니다.");
            });
    }, []);


    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const response = await api.get("/members/me");
                const userData = response.data;
                setUser(userData);

                // 👉 사용자 부서 코드로 기본 선택값 설정
                if (userData.deptId) {

                    setSelectedDeptId(userData.deptId);
                }
            } catch (err) {
                console.error("사용자 정보 불러오기 실패:", err);
                setError("사용자 정보를 불러오는 데 실패했습니다.");
            }
        };

        fetchUserInfo();
    }, []);


    const colorPalette = [
        "#60a5fa", // 파랑
        "#facc15", // 노랑
        "#34d399", // 초록
        "#f87171", // 빨강
        "#c084fc", // 보라
        "#fb923c", // 주황
        "#4ade80", // 연두
    ];

// 이름에 따라 고유한 색 인덱스 반환
    const getColorByName = (name) => {
        let hash = 0;
        for (let i = 0; i < name.length; i++) {
            hash = name.charCodeAt(i) + ((hash << 5) - hash);
        }
        const index = Math.abs(hash) % colorPalette.length;
        return colorPalette[index];
    };

    const handleDatesSet = (info) => {
        const currentDate = info.view.currentStart; // 또는 info.start도 됨

        const year = currentDate.getFullYear();
        const month = String(currentDate.getMonth() + 1).padStart(2, "0"); // 0부터 시작하므로 +1

        const yearMonth = `${year}-${month}`;

        const fetchKey = `${yearMonth}-${selectedDeptId}`;

        if (lastFetchedMonth.current === fetchKey) return;
        lastFetchedMonth.current = fetchKey;

        // API 호출
        api
            .get("/vacations/calendar", {
                params: {
                    yearMonth: yearMonth,
                    deptId: selectedDeptId,
                },
            })
            .then((res) => {
                const formatted = res.data.map((vacation) => {
                    const from = vacation.from;
                    const to = vacation.to;

                    const formatDate = (date) => {
                        const yyyy = date.getFullYear();
                        const mm = String(date.getMonth() + 1).padStart(2, "0");
                        const dd = String(date.getDate()).padStart(2, "0");
                        return `${yyyy}-${mm}-${dd}`;
                    };

                    const startDate = new Date(from);
                    const endDate = new Date(to);

                    const startDateStr = formatDate(startDate); // ✅ Date 객체로 넘겨야 함
                    const endDateStr = formatDate(endDate);
                    // 시각만 뽑기
                    const getTimeString = (date) => {
                        const hour = String(date.getHours()).padStart(2, "0");
                        const minute = String(date.getMinutes()).padStart(2, "0");
                        return `${hour}시${minute !== "00" ? ` ${minute}분` : ""}`;
                    };

                    // ✅ 오전/오후 판별 (반차일 때만)
                    let prefixText = "";
                    if (vacation.typeName === "반차") {
                        const hour = startDate.getHours();
                        if (hour <= 13) {
                            prefixText = "오전 ";
                        } else {
                            prefixText = "오후 ";
                        }
                    }

                    const [start, endRaw] = startDate <= endDate
                        ? [startDateStr, endDateStr]
                        : [endDateStr, startDateStr];

                    return {
                        title: `${vacation.name} ${vacation.positionName} (${prefixText}${vacation.typeName})`,
                        start,
                        end: plusOneDay(endRaw), // ✅ 하루 더해서 inclusive하게 표시되도록
                        allDay: true,
                        color: getColorByName(vacation.name),
                    };
                });
                setEvents(formatted);
            })
            .catch((err) => {
                console.error("일정 데이터 불러오기 실패", err);
            });
    };

    const plusOneDay = (dateStr) => {
        const date = new Date(dateStr);
        date.setDate(date.getDate() + 1);
        return date.toISOString().slice(0, 10);
    };

    const calendarRef = useRef(null);

    useEffect(() => {
        const calendarApi = calendarRef.current?.getApi();
        if (calendarApi) {
            const currentDate = calendarApi.getDate();
            handleDatesSet({ view: { currentStart: currentDate } });
        }
    }, [selectedDeptId]);

    return (
        <div style={{ height: "800px" , margin: "20px"}}>
            <select
                value={selectedDeptId}
                onChange={(e) => setSelectedDeptId(Number(e.target.value))}
                style={{
                    fontSize: "16px",
                    fontWeight: "bold",
                    padding: "8px 12px",
                    border: "2px solid #7a807c",
                    borderRadius: "8px",
                    outline: "none",
                    marginBottom: "16px",
                    cursor: "pointer",
                }}
            >

            <option value={0}>전체</option> {}
            {depts.map((dept) => (
                <option key={dept.id} value={dept.id}>
                    {dept.name}
                </option>
            ))}
            </select>
            <FullCalendar
                ref={calendarRef}
                plugins={[dayGridPlugin, interactionPlugin]}
                initialView="dayGridMonth"
                events={events}
                datesSet={handleDatesSet} // 👈 이게 핵심
                height="100%"
            />
        </div>
    );
}



export default Calendar;




