/*
 * =====================================================================================
 *
 *       Filename:  test.cpp
 *
 *    Description:  unit test entry
 *
 *        Version:  1.0
 *        Created:  2011/02/18 10ʱ15��56��
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  liang.chenl 
 *        Company:  
 *
 * =====================================================================================
 */

#include "def.h"

int main(int argc, char** argv) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
