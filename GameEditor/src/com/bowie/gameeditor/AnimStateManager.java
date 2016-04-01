package com.bowie.gameeditor;

import java.util.ArrayList;
import java.util.List;

import com.bowie.gameeditor.SkAnim.Action;

public class AnimStateManager {
	public static final int ANIMSTATE_IDLE = 0;
	public static final int ANIMSTATE_MOVING = 1;
	public static final int ANIMSTATE_SLIDE_TO_STOP = 2;
	
	//==================================================================================================
	// base animation state class
	public class AnimState {
		public int id;
		public float enterSpeed;
		
		public AnimState() {
			id = -1;
			enterSpeed = 0;
		}
		
		public void update(float dt) {}
		public void preRender(float dt) {}
		public void setMoveSpeed(float s) {}
		public void setup(Skeleton skel) {}
		public SkPose getRenderPose() {
			return null;
		}
	}
	
	// Idle state
	public class LoopAnimState extends AnimState {
		public float phase = 0;
		public float phaseSpd = 0;
		public float renderPhase = 0;
		public String actionName;
		public int actionId;
		public SkPose renderPose;
		
		public float [] frameTime = {0, 0};
		
		public LoopAnimState(int id, float enterSpeed, String actionName) {
			this.id = id;
			this.enterSpeed = enterSpeed;
			this.actionName = actionName;
		}
		
		@Override
		public void setup(Skeleton skel) {
			actionId = skel.getAnimation().getActionId(actionName);
			Action act = skel.getAnimation().getActionById(actionId);
			if (act != null) {
				this.phaseSpd = 60.0f/ act.getFrameCount();
				act.getTrackTime(frameTime);
			}
			
			renderPose = new SkPose(skel);
		}
		
		@Override
		public void update(float dt) {
			this.phase += this.phaseSpd * dt;
			this.phase = MathHelper.wrapFloat01(phase);
			
//			System.out.println("AS_LOOP: " + phase);
		}
		
		@Override
		public void preRender(float dt) {
			this.renderPhase = this.phase + this.phaseSpd * dt;
			this.renderPhase = MathHelper.wrapFloat01(renderPhase);
			
			// calculate pose
			this.renderPose.calculateCubic(actionId, MathHelper.phaseToRenderTime(renderPhase, frameTime));
		}
		
		@Override
		public SkPose getRenderPose() {
			return this.renderPose;
		}
	}
	
	// Locomotive state
	public class LocAnimState extends AnimState {
		public String standName;
		public String walkName;
		public String runName;
		public float walkSpeed;
		public float runSpeed;
		
		public int standId;
		public int walkId;
		public int runId;
		
		public float phase = 0;
		public float phaseSpeed = 0;	// will change depending on speed
		public float renderPhase = 0;
		
		public float [] standTrackTime = {0, 0};
		public float [] walkTrackTime = {0, 0};
		public float [] runTrackTime = {0, 0};
		
		public float moveSpeed = 0;
		public float interp = 0;
		
		public static final int STAND_TO_WALK = 0;
		public static final int WALK_TO_RUN = 1;
		public static final int RUN_FASTER = 2;
		
		public int status = STAND_TO_WALK;
		
		// we keep 3 pose
		// from, to, final
		public SkPose poseFrom;
		public SkPose poseTo;
		public SkPose renderPose;	// final
		
		// 3 separate phase speed (stand, walk, run)
		public float [] phaseSpeeds = {0, 0, 0};
		
		public LocAnimState(int id, String standName, String walkName, String runName, float walkSpeed, float runSpeed) {
			this.id = id;
			this.enterSpeed = 3.0f;
			this.standName = standName;
			this.walkName = walkName;
			this.runName = runName;
			this.walkSpeed = walkSpeed;
			this.runSpeed = runSpeed;
		}	
		
		@Override
		public void setMoveSpeed(float s) {
			this.moveSpeed = s;
		}
		
		@Override
		public void update(float dt) {
			// calculate phase speed
			if (moveSpeed <= walkSpeed) {
				// stand <-> walk
				status = STAND_TO_WALK;
				interp = moveSpeed/walkSpeed;
				phaseSpeed = (1.0f-interp) * phaseSpeeds[0] + interp * phaseSpeeds[1];
			} else if (moveSpeed > walkSpeed && moveSpeed <= runSpeed) {
				// walk <-> run
				status = WALK_TO_RUN;
				interp = (moveSpeed - walkSpeed) / (runSpeed - walkSpeed);
				phaseSpeed = (1.0f-interp) * phaseSpeeds[1] + interp * phaseSpeeds[2];
			} else {
				// run -> run faster
				status = RUN_FASTER;
				interp = moveSpeed / runSpeed;
				phaseSpeed = phaseSpeeds[2] * interp;	// multiply
			}
			
			// update as usual
			phase += phaseSpeed * dt;
			phase = MathHelper.wrapFloat01(phase);
			
//			System.out.println("AS_LOC: " + status + " , " + phase + " , " + interp);
		}
		
		@Override
		public void preRender(float dt) {
			this.renderPhase = this.phase + this.phaseSpeed * dt;
			this.renderPhase = MathHelper.wrapFloat01(renderPhase);
			
			// depending on what states of animation we are
			if (status == STAND_TO_WALK) {
				poseFrom.calculateCubic(standId, MathHelper.phaseToRenderTime(renderPhase, standTrackTime));
				poseTo.calculateCubic(walkId, MathHelper.phaseToRenderTime(renderPhase, walkTrackTime));
				
				// blend em
				SkPose.blendPose(poseFrom, poseTo, interp, renderPose);
			} else if (status == WALK_TO_RUN) {
				poseFrom.calculateCubic(walkId, MathHelper.phaseToRenderTime(renderPhase, walkTrackTime));
				poseTo.calculateCubic(runId, MathHelper.phaseToRenderTime(renderPhase, runTrackTime));
				
				// blend em
				SkPose.blendPose(poseFrom, poseTo, interp, renderPose);
			} else {
				// mmust be run faster
				renderPose.calculateCubic(runId, MathHelper.phaseToRenderTime(renderPhase, runTrackTime));
			}
		}
		
		@Override
		public SkPose getRenderPose() {
			return renderPose;
		}
		
		@Override
		public void setup(Skeleton skel) {
			standId = skel.getAnimation().getActionId(standName);
			walkId = skel.getAnimation().getActionId(walkName);
			runId = skel.getAnimation().getActionId(runName);
			
			Action actStand = skel.getAnimation().getActionById(standId);
			Action actWalk = skel.getAnimation().getActionById(walkId);
			Action actRun = skel.getAnimation().getActionById(runId);
			
			actStand.getTrackTime(standTrackTime);
			actWalk.getTrackTime(walkTrackTime);
			actRun.getTrackTime(runTrackTime);
			
			this.phaseSpeeds[0] = 60.0f/actStand.getFrameCount();
			this.phaseSpeeds[1] = 60.0f/actWalk.getFrameCount();
			this.phaseSpeeds[2] = 60.0f/actRun.getFrameCount();
			
			poseFrom = new SkPose(skel);
			poseTo = new SkPose(skel);
			renderPose = new SkPose(skel);
		}
	}
	
	// slide animation
	public class SlideAnimState extends AnimState {
		public float moveSpeed;	// for interpolation states
		public float phase = 0;
		public float lastPhase = 0;	// derive speed from here
		public float renderPhase = 0;
		public String actionName;
		public int actionId;
		
		public float [] trackTime = {0,0};
		
		public float slideStartSpeed;
		public float slideEndSpeed;
		
		public SkPose renderPose;
		
		public SlideAnimState(int id, String actionName, float slideStartSpeed, float slideEndSpeed) {
			this.id = id;
			this.enterSpeed = 20.0f;
			this.slideStartSpeed = slideStartSpeed;
			this.slideEndSpeed = slideEndSpeed;
			this.actionName = actionName;
		}
		
		@Override
		public void setup(Skeleton skel) {
			this.actionId = skel.getAnimation().getActionId(actionName);
			Action slideAction = skel.getAnimation().getActionById(actionId);
			
			slideAction.getTrackTime(trackTime);
			renderPose = new SkPose(skel);
		}
		
		@Override
		public void update(float dt) {
			lastPhase = phase;
			// slideEndSpeed ....... 1
			// slideStartSpeed ...... 0
			// calculate phase
			phase = (moveSpeed - slideStartSpeed) / (slideEndSpeed - slideStartSpeed);
			// cap em
			phase = phase < Vector3.EPSILON ? Vector3.EPSILON : phase >= (1-Vector3.EPSILON) ? (1-Vector3.EPSILON) : phase;
			
//			System.out.println("AS_SLIDE: " + phase);
		}
		
		@Override
		public void preRender(float dt) {
			// interpolate, calculate pose
			renderPhase = phase + (phase-lastPhase) * dt;
			renderPhase = renderPhase < 0 ? 0 : renderPhase > 1 ? 1 : renderPhase;
			
			// now do it
			renderPose.calculateLinear(actionId, MathHelper.phaseToRenderTime(renderPhase, trackTime));
		}
		
		@Override
		public SkPose getRenderPose() {
			return renderPose;
		}
		
		@Override
		public void setMoveSpeed(float s) {
			this.moveSpeed = s;
		}
	}
	
	//==================================================================================================
	
	public AnimStateManager() {
		initStates();
	}
	
	public AnimStateManager(Skeleton skel) {
		initStates();
		setupData(skel);
	}
	
	public void initStates() {
		// init all states
		states = new ArrayList<>();
		
		// the idle state
		states.add(new LoopAnimState(ANIMSTATE_IDLE, 2.0f, "idle"));
		
		// the move state
		states.add(new LocAnimState(ANIMSTATE_MOVING, "stand", "walk", "run", 1.5f, 4.5f));
		
		// the slide
		states.add(new SlideAnimState(ANIMSTATE_SLIDE_TO_STOP, "slide2stop", 5.0f, 0.0f));
		
		// enter first state
		this.changeTo(ANIMSTATE_IDLE);
	}
	
	public void setupData(Skeleton skel) {
		if (skel == null)
			return;
		
		for (AnimState s : states) {
			s.setup(skel);
		}
		
		// also allocate blend pose
		finalPose = new SkPose(skel);
	}
	
	public void update(float dt) {
		// update active state
		if (curState != null)
			curState.update(dt);
		if (nextState != null)
			nextState.update(dt);
		
		// now, we advance
		interpolation += interpSpeed * dt;
		
		// are we moving to next, or what?
		if (interpolation >= 1.0f) {
			// we have arrived to next state
			curState = nextState;
			nextState = null;
			interpSpeed = 0;
			interpolation = 0;
		} else if (interpolation <= 0.0f) {
			// we have to move to starting state
			interpSpeed = 0;
			interpolation = 0;
			nextState = null;
		}
		
		int cs = -1;
		int ns = -1;
		
		if (curState != null)
			cs = curState.id;
		if (nextState != null)
			ns = nextState.id;
		System.out.println("AnimState: " + cs + " ~ " + ns + " @ " + interpolation);
	}
	
	public void changeTo(int state) {
		// set logic here....
		
		// for now, change abruptly
		if (state < states.size()) {
			AnimState next = states.get(state);
			
			// do some logic
			if (next == curState) {
				// we're moving to current state?
				// no, flip speed
				interpSpeed = -next.enterSpeed;
			} else if (next == nextState) {
				// well simply set speed
				interpSpeed = next.enterSpeed;
			} else {
				// new state
				nextState = next;
				interpSpeed = next.enterSpeed;
			}
		}
	}
	
	public void setMoveSpeed(float spd) {
		// set the speed
		if (curState != null) {
			curState.setMoveSpeed(spd);
		}
		
		if (nextState != null) {
			nextState.setMoveSpeed(spd);
		}
	}
	
	public SkPose getRenderPose(float dt) {
		// must generate pose now!!
		return finalPose;
	}
	
	public void preRender(float dt) {
		r_interpolation = interpolation + interpSpeed * dt;
		// clamp
		r_interpolation = r_interpolation < 0 ? 0 : r_interpolation > 1 ? 1 : r_interpolation;
		// pre render both states
		if (curState != null)
			curState.preRender(dt);
		if (nextState != null)
			nextState.preRender(dt);
		
		// okay, calculate blend pose now?
		if (curState != null && nextState == null) {
			// no blending
			finalPose.setTo(curState.getRenderPose());
		} else if (curState != null && nextState != null) {
			// blend now!!
			SkPose.blendPose(curState.getRenderPose(), nextState.getRenderPose(), r_interpolation, finalPose);
		}
	}
	
	// state machine data
	protected AnimState curState = null;
	protected AnimState nextState = null;
	protected float interpolation = 0;
	protected float r_interpolation = 0;
	protected float interpSpeed = 0;
	
	protected SkPose finalPose;
	
	// holds all states here
	protected List<AnimState> states;
}
