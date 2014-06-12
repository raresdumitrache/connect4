/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// OpenGL ES 2.0 code

#include <jni.h>

#include <android/log.h>
#include <android/input.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libgl2jni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

// translate & scale points to our coordinate system
#define TRANSFORM_COORD(x, y)				\
{											\
	y = (height-y)*70.0f/height - 35.0f;	\
	x = x*50.0f/width - 25.0f;				\
}

#define SET_COLOR_3f(array, index, color_array) {	\
	array[index]	= color_array[0];				\
	array[index+1]	= color_array[1];				\
	array[index+2]	= color_array[2];			}

static const char gVertexShader[] = 
	"uniform mat4 p_matrix;\n"
	"uniform mat4 mv_matrix;\n"
	"attribute vec4 vPosition;\n"
	"attribute vec4 aColor;\n"
	"varying vec4 vColor;"
	"void main() {\n"
    "  gl_Position = p_matrix * mv_matrix * vPosition;\n"
	"  vColor = aColor;"
    "}\n";

static const char gFragmentShader[] = 
    "precision mediump float;\n"
	"varying lowp vec4 vColor;\n"
	"void main() {\n"
    "  gl_FragColor = vColor;\n"
    "}\n";

//vec4(0, 1, 0, 1);\n"

GLfloat p_matrix[16], mv_matrix[16];

#define FOV_R 1.57f
#define NEAR 0.5f
#define FAR 16.0f

#define GRID_LINES			7
#define GRID_COLUMNS		8
#define GRID_MIN_X			(-25.0f)
#define GRID_MAX_X			25.0f
#define GRID_MIN_Y			(-35.0f)
#define GRID_MAX_Y			20.0f
#define MY_WIDTH			(GRID_MAX_X-GRID_MIN_X)
#define MY_HEIGHT			(GRID_MAX_Y-GRID_MIN_Y)
#define LINE_STEP			MY_HEIGHT/(GRID_LINES-1.0f)
#define COLUMN_STEP			MY_WIDTH/(GRID_COLUMNS-1.0f)
#define RADIUS				COLUMN_STEP/2.0f * 0.9f
#define CENTER_X_PLAYER_0	GRID_MIN_X + COLUMN_STEP/2.0f
#define CENTER_Y_PLAYER_0	GRID_MAX_Y + LINE_STEP/2.0f
#define CENTER_X_PLAYER_1	GRID_MAX_X - COLUMN_STEP/2.0f
#define CENTER_Y_PLAYER_1	GRID_MAX_Y + LINE_STEP/2.0f
#define MAX_PIECES			21 // (6*7)/2 - 6x7 board, 2 players

static GLfloat grid[GRID_COLUMNS*4];

// (2 players) x (max. pieces on board) x (each piece has 2 coordinates)
static GLfloat player_pieces[2][MAX_PIECES][2] = {0.0f};
static GLint pieces_on_board[2] = {0};
static GLfloat player_piece_centers[2][2] = {
										{CENTER_X_PLAYER_0, CENTER_Y_PLAYER_0},
										{CENTER_X_PLAYER_1, CENTER_Y_PLAYER_1}};

// index of current player (0 or 1)
static GLint player_in_turn = 0;

static GLfloat player_color[2][3] = {{0.6f, 0.1f, 0.1f},	// player 0's color
									{0.6f, 0.6f, 0.0f}};	// player 1's color
static GLfloat grid_color[3] = {0.2f, 0.25f, 0.3f};


GLuint gProgram;
GLuint gvPositionHandle;
GLuint gvColorHandle;
GLuint gPmatrix;
GLuint gMVmatrix;

GLfloat eye_position[3] = { 0.0f, 0.0f, 16.0f };

// rotation angles on all 3 axis
GLfloat r_x = 0.0f, r_y = 0.0f, r_z = 0.0f;

// translation steps for all 3 axes
GLfloat t_x = 0.0f, t_y = 0.0f, t_z = 0.0f;

// real screen size
float  width = 0, height = 0;

// coordinates of screen touch
float touch_x, touch_y;
bool dragging = false;

GLuint load_shader(GLenum shaderType, const char* pSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char* buf = (char*) malloc(infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d:\n%s\n",
                            shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint create_program(const char* pVertexSource, const char* pFragmentSource) {
    GLuint vertexShader = load_shader(GL_VERTEX_SHADER, pVertexSource);

    if (!vertexShader)
        return 0;

    GLuint pixelShader = load_shader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);

        glAttachShader(program, pixelShader);
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char* buf = (char*) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);

                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

//camera to screen matrix
static void set_perspective_matrix (GLfloat * matrix, GLfloat w, GLfloat h)
{
  GLfloat r_xy_factor = fminf (w, h) / FOV_R;
  GLfloat r_x = r_xy_factor / w,
		  r_y = r_xy_factor / h;
  GLfloat r_zw_factor = 1.0f / (FAR - NEAR);
  GLfloat r_z = (NEAR + FAR) * r_zw_factor;
  GLfloat r_w = -2.0f * NEAR * FAR * r_zw_factor;

  matrix[0] = r_x;
  matrix[1] = 0.0f;
  matrix[2] = 0.0f;
  matrix[3] = 0.0f;

  matrix[4] = 0.0f;
  matrix[5] = r_y;
  matrix[6] = 0.0f;
  matrix[7] = 0.0f;

  matrix[8] = 0.0f;
  matrix[9] = 0.0f;
  matrix[10] = r_z;
  matrix[11] = 1.0f;

  matrix[12] = 0.0f;
  matrix[13] = 0.0f;
  matrix[14] = r_w;
  matrix[15] = 0.0f;
}

static void set_model_view_matrix(GLfloat *matrix)
{
	GLfloat sin_x = sin(r_x);
	GLfloat cos_x = cos(r_x);

	GLfloat sin_y = sin(r_y);
	GLfloat cos_y = cos(r_y);

	GLfloat sin_z = sin(r_z);
	GLfloat cos_z = cos(r_z);

	matrix[0] = cos_x * cos_y;
	matrix[1] = sin_x * cos_y;
	matrix[2] = -sin_y;
	matrix[3] = 0.0f;

	matrix[4] = cos_x * sin_y * sin_z - sin_x * cos_z;
	matrix[5] = cos_x * cos_z + sin_x * sin_y * sin_z;
	matrix[6] = sin_z * cos_y;
	matrix[7] = 0.0f;

	matrix[8] = cos_x * sin_y * cos_z + sin_x * sin_z;
	matrix[9] = sin_x * sin_y * cos_z - cos_x * sin_z;
	matrix[10] = cos_y * cos_z;
	matrix[11] = 0.0f;

	matrix[12] = eye_position[0];
	matrix[13] = eye_position[1];
	matrix[14] = eye_position[2];
	matrix[15] = 1.0f;
}

bool setup_graphics(int w, int h) {

    gProgram = create_program(gVertexShader, gFragmentShader);

    if (!gProgram)
        return false;

    gPmatrix			= glGetUniformLocation(gProgram, "p_matrix");
    gMVmatrix			= glGetUniformLocation(gProgram, "mv_matrix");
    gvPositionHandle	= glGetAttribLocation(gProgram, "vPosition");
    gvColorHandle		= glGetAttribLocation(gProgram, "aColor");

    LOGI("color handle = %i, pos handle = %i", gvColorHandle, gvPositionHandle);

    glViewport(0, 0, w, h);

    set_model_view_matrix(mv_matrix);
    set_perspective_matrix(p_matrix, w, h);

    width	= w;
    height	= h;

    // initialize first player's piece position
    touch_x	= player_piece_centers[player_in_turn][0];
    touch_y	= player_piece_centers[player_in_turn][1];

    return true;
}

static void draw_grid()
{
	GLfloat dist_x		= GRID_MAX_X - GRID_MIN_X,
			dist_y		= GRID_MAX_Y - GRID_MIN_Y,
			line_end_x, line_end_y, delta;
	GLint i, color_index;
	GLfloat color[GRID_COLUMNS * 2 * 3]; // GRID_COLUMNS > GRID_LINES

	// horizontal borders
	line_end_x	= GRID_MIN_X;
	line_end_y	= GRID_MIN_Y;
	delta		= dist_x;
	color_index	= 0;
	for (i=0; i<GRID_LINES*4;i+=4) {
		// first end of line (x,y)
		grid[i]		= line_end_x;
		grid[i+1]	= line_end_y;

		line_end_x	+= delta;

		// second end of line (x,y)
		grid[i+2]	= line_end_x;
		grid[i+3]	= line_end_y;

		line_end_y	+= LINE_STEP;
		delta		*= -1;

		// set color for this line ()
		SET_COLOR_3f(color, color_index, grid_color);
		SET_COLOR_3f(color, color_index+3, grid_color);

		color_index	+= 6;
	}

	glVertexAttribPointer(gvColorHandle, 3, GL_FLOAT, GL_FALSE, 0, color);
	glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, grid);
	glDrawArrays(GL_LINE_STRIP, 0, GRID_LINES * 2);

	// vertical borders
	line_end_x	= GRID_MIN_X;
	line_end_y	= GRID_MIN_Y;
	delta		= dist_y;
	color_index	= 0;
	for (i=0; i<GRID_COLUMNS*4;i+=4) {
		// first end of line (x,y)
		grid[i]		= line_end_x;
		grid[i+1]	= line_end_y;

		line_end_y	+= delta;

		// second end of line (x,y)
		grid[i+2]	= line_end_x;
		grid[i+3]	= line_end_y;

		line_end_x	+= COLUMN_STEP;
		delta		*= -1;

		// set color for this line ()
		SET_COLOR_3f(color, color_index, grid_color);
		SET_COLOR_3f(color, color_index+3, grid_color);

		color_index	+= 6;
	}

	glVertexAttribPointer(gvColorHandle, 3, GL_FLOAT, GL_FALSE, 0, color);
	glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, grid);
	glDrawArrays(GL_LINE_STRIP, 0, GRID_COLUMNS * 2);
}

static void draw_circle(GLfloat center_x, GLfloat center_y, GLfloat radius,
		GLfloat color[3], GLfloat center_color[3])
{
	GLfloat circle[360 * 4 + 2], x, y;
	GLfloat circle_color[360*6 + 3];
	GLint i, color_index = 0;

	for (i=0; i<360*4; i+=4) {
		circle[i]	= radius * cos(i/4.0f * 0.0174532f) + center_x;
		circle[i+1]	= radius * sin(i/4.0f * 0.0174532f) + center_y;

		circle[i+2]	= center_x;
		circle[i+3]	= center_y;

		// set colors
		SET_COLOR_3f(circle_color, color_index, color);
		SET_COLOR_3f(circle_color, color_index + 3, center_color);
		color_index += 6;
	}

	circle[i]	= center_x + radius;
	circle[i+1]	= center_y;
	SET_COLOR_3f(circle_color, color_index, color);

	glVertexAttribPointer(gvColorHandle, 3, GL_FLOAT, GL_FALSE, 0, circle_color);
	glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, circle);
	glDrawArrays(GL_TRIANGLE_STRIP, 0, 360*2+1);
}

static void draw_players_pieces()
{
	int player, piece, i;
	float c_color[3];

	player = player_in_turn;
	for (i=0; i<2; i++) {
		c_color[0]	= player_color[player][0] + 0.2f;
		c_color[1]	= player_color[player][1] + 0.2f;
		c_color[2]	= player_color[player][2] + 0.2f;

		piece = 0;
		while (piece < pieces_on_board[player]) {
			draw_circle(player_pieces[player][piece][0],
						player_pieces[player][piece][1], RADIUS,
						player_color[player],
						c_color);

			piece++;
		}

		player = 1 - player;
	}
}

static void draw_pieces_to_play()
{
	int other_player	= 1 - player_in_turn;

	// draw piece of the current player
	GLfloat center_color[3]	= {0.6f, 0.6f, 0.6f};
	draw_circle(touch_x, touch_y, RADIUS, player_color[player_in_turn],
				center_color);

	// draw piece of the other player
	center_color[0] = player_color[other_player][0] - 0.2f;
	center_color[1] = player_color[other_player][1] - 0.2f;
	center_color[2] = player_color[other_player][2] - 0.2f;

	draw_circle(player_piece_centers[other_player][0],
			player_piece_centers[other_player][1],
			RADIUS,
			player_color[other_player],
			center_color);
}


void render_frame() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    glUseProgram(gProgram);

    glUniformMatrix4fv(gPmatrix, 1, GL_FALSE, p_matrix);
    glUniformMatrix4fv(gMVmatrix, 1, GL_FALSE, mv_matrix);

    glEnableVertexAttribArray(gvPositionHandle);
    glEnableVertexAttribArray(gvColorHandle);

    draw_grid();
    draw_players_pieces();
    draw_pieces_to_play();
}

// Place the piece on board
void valid_move(int line, int column)
{
	// add piece on board
	int turn = player_in_turn;

	player_pieces[turn][pieces_on_board[turn]][0]	= (column+0.5f)*COLUMN_STEP+GRID_MIN_X;
	player_pieces[turn][pieces_on_board[turn]][1]	= (line+0.5f)*LINE_STEP+GRID_MIN_Y;

	// update number of player's pieces on board
	pieces_on_board[turn]++;

	// switch turns
	player_in_turn = 1 - turn;

	// update touch_x, touch_y
	touch_x	= player_piece_centers[player_in_turn][0];
	touch_y	= player_piece_centers[player_in_turn][1];
}

// Put the piece back in its starting point
void invalid_move()
{
	touch_x	= player_piece_centers[player_in_turn][0];
	touch_y	= player_piece_centers[player_in_turn][1];
}

/* Called upon the end of a drag & drop move of the player.
 *
 * It returns the column on which the player dragged the piece (0..6) or -1 if:
 *   - the drop place was not on the board
 *   - the piece is still being dragged
 */
int touch_screen(float x, float y, bool drop)
{
	TRANSFORM_COORD(x, y);

	if (drop) {
		/* Compute column - check only if it does not go above the board
		 * (it is the only possible case since we placed the board on the lower
		 * screen and full-width)
		 */
		float column;

		if (y > GRID_MAX_Y) {
			invalid_move();
			return -1;
		}

		column = (x-GRID_MIN_X);
		column /= COLUMN_STEP;

		return (int)column;
	} else {
		// update dragging position
		touch_x	= x;
		touch_y	= y;
	}

	return -1;
}

bool spot_on(float x, float y)
{
	GLfloat center_x = player_piece_centers[player_in_turn][0],
			center_y = player_piece_centers[player_in_turn][1];

	TRANSFORM_COORD(x, y);

	if (center_x + RADIUS >= x  && center_x - RADIUS <= x &&
		center_y + RADIUS >= y  && center_y - RADIUS <= y)
		return true;

	return false;
}

extern "C" {
    JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_init(JNIEnv * env, jobject obj,  jint width, jint height);
    JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_step(JNIEnv * env, jobject obj);
    JNIEXPORT jint JNICALL Java_com_android_gl2jni_GL2JNILib_touch(JNIEnv * env, jobject obj, jfloat x, jfloat y, jboolean drop);
    JNIEXPORT jboolean JNICALL Java_com_android_gl2jni_GL2JNILib_spotOn(JNIEnv * env, jobject obj, jfloat x, jfloat y);
    JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_validMove(JNIEnv * env, jobject obj,  jint line, jint column);
    JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_invalidMove(JNIEnv * env, jobject obj);
};

JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_init(JNIEnv * env, jobject obj,  jint width, jint height)
{
    setup_graphics(width, height);
}

JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_step(JNIEnv * env, jobject obj)
{
    render_frame();
}

JNIEXPORT jint JNICALL Java_com_android_gl2jni_GL2JNILib_touch(JNIEnv * env, jobject obj, jfloat x, jfloat y, jboolean drop)
{
	return touch_screen(x, y, drop);
}

JNIEXPORT jboolean JNICALL Java_com_android_gl2jni_GL2JNILib_spotOn(JNIEnv * env, jobject obj, jfloat x, jfloat y)
{
	return spot_on(x, y);
}

JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_validMove(JNIEnv * env, jobject obj,  jint line, jint column)
{
	valid_move(line, column);
}

JNIEXPORT void JNICALL Java_com_android_gl2jni_GL2JNILib_invalidMove(JNIEnv * env, jobject obj)
{
	invalid_move();
}
