package programmers.team6.support;

import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.member.enums.GroupCode;
import programmers.team6.domain.vacation.enums.VacationCode;

public enum TestVacationType {

	ANNUAL {
		@Override
		public Code toCode() {
			return new Code(GroupCode.VACATION_TYPE.getCode(), VacationCode.ANNUAL.getCode(), VacationCode.ANNUAL.name());
		}
	},
	REWARD {
		@Override
		public Code toCode() {
			return new Code(GroupCode.VACATION_TYPE.getCode(), VacationCode.REWARD.getCode(), VacationCode.REWARD.name());
		}
	},
	OFFICIAL {
		@Override
		public Code toCode() {
			return new Code(GroupCode.VACATION_TYPE.getCode(), VacationCode.OFFICIAL.getCode(), VacationCode.OFFICIAL.name());
		}
	},
	CONGRATULATORY {
		@Override
		public Code toCode() {
			return new Code(GroupCode.VACATION_TYPE.getCode(), VacationCode.CONGRATULATORY.getCode(), VacationCode.CONGRATULATORY.name());
		}
	},
	HALP {
		@Override
		public Code toCode() {
			return new Code(GroupCode.VACATION_TYPE.getCode(),"05", "반차");
		}
	};

	public abstract Code toCode();
}
