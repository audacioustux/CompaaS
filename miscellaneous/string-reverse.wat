(module
  (type (;0;) (func (result i32)))
  (type (;1;) (func))
  (type (;2;) (func (param i32 i32)))
  (type (;3;) (func (param i32)))
  (type (;4;) (func (param i32) (result i32)))
  (func (;0;) (type 1)
    call 6)
  (func (;1;) (type 2) (param i32 i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32 i32)
    global.get 0
    local.set 2
    i32.const 32
    local.set 3
    local.get 2
    local.get 3
    i32.sub
    local.set 4
    local.get 4
    local.get 0
    i32.store offset=28
    local.get 4
    local.get 1
    i32.store offset=24
    i32.const 0
    local.set 5
    local.get 4
    local.get 5
    i32.store offset=20
    local.get 4
    i32.load offset=24
    local.set 6
    i32.const 1
    local.set 7
    local.get 6
    local.get 7
    i32.sub
    local.set 8
    local.get 4
    local.get 8
    i32.store offset=16
    block  ;; label = @1
      loop  ;; label = @2
        local.get 4
        i32.load offset=20
        local.set 9
        local.get 4
        i32.load offset=16
        local.set 10
        local.get 9
        local.set 11
        local.get 10
        local.set 12
        local.get 11
        local.get 12
        i32.lt_s
        local.set 13
        i32.const 1
        local.set 14
        local.get 13
        local.get 14
        i32.and
        local.set 15
        local.get 15
        i32.eqz
        br_if 1 (;@1;)
        local.get 4
        i32.load offset=28
        local.set 16
        local.get 4
        i32.load offset=20
        local.set 17
        local.get 16
        local.get 17
        i32.add
        local.set 18
        local.get 18
        i32.load8_u
        local.set 19
        local.get 4
        local.get 19
        i32.store8 offset=15
        local.get 4
        i32.load offset=28
        local.set 20
        local.get 4
        i32.load offset=16
        local.set 21
        local.get 20
        local.get 21
        i32.add
        local.set 22
        local.get 22
        i32.load8_u
        local.set 23
        local.get 4
        i32.load offset=28
        local.set 24
        local.get 4
        i32.load offset=20
        local.set 25
        local.get 24
        local.get 25
        i32.add
        local.set 26
        local.get 26
        local.get 23
        i32.store8
        local.get 4
        i32.load8_u offset=15
        local.set 27
        local.get 4
        i32.load offset=28
        local.set 28
        local.get 4
        i32.load offset=16
        local.set 29
        local.get 28
        local.get 29
        i32.add
        local.set 30
        local.get 30
        local.get 27
        i32.store8
        local.get 4
        i32.load offset=20
        local.set 31
        i32.const 1
        local.set 32
        local.get 31
        local.get 32
        i32.add
        local.set 33
        local.get 4
        local.get 33
        i32.store offset=20
        local.get 4
        i32.load offset=16
        local.set 34
        i32.const -1
        local.set 35
        local.get 34
        local.get 35
        i32.add
        local.set 36
        local.get 4
        local.get 36
        i32.store offset=16
        br 0 (;@2;)
      end
      unreachable
    end
    return)
  (func (;2;) (type 1)
    block  ;; label = @1
      i32.const 1
      i32.eqz
      br_if 0 (;@1;)
      call 0
    end)
  (func (;3;) (type 0) (result i32)
    global.get 0)
  (func (;4;) (type 3) (param i32)
    local.get 0
    global.set 0)
  (func (;5;) (type 4) (param i32) (result i32)
    (local i32 i32)
    global.get 0
    local.get 0
    i32.sub
    i32.const -16
    i32.and
    local.tee 1
    global.set 0
    local.get 1)
  (func (;6;) (type 1)
    i32.const 5243920
    global.set 2
    i32.const 1028
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    global.set 1)
  (func (;7;) (type 0) (result i32)
    global.get 0
    global.get 1
    i32.sub)
  (func (;8;) (type 0) (result i32)
    global.get 2)
  (func (;9;) (type 0) (result i32)
    global.get 1)
  (func (;10;) (type 0) (result i32)
    i32.const 1024)
  (table (;0;) 2 2 funcref)
  (memory (;0;) 256 256)
  (global (;0;) (mut i32) (i32.const 5243920))
  (global (;1;) (mut i32) (i32.const 0))
  (global (;2;) (mut i32) (i32.const 0))
  (export "memory" (memory 0))
  (export "reverse" (func 1))
  (export "_initialize" (func 2))
  (export "__indirect_function_table" (table 0))
  (export "__errno_location" (func 10))
  (export "emscripten_stack_init" (func 6))
  (export "emscripten_stack_get_free" (func 7))
  (export "emscripten_stack_get_base" (func 8))
  (export "emscripten_stack_get_end" (func 9))
  (export "stackSave" (func 3))
  (export "stackRestore" (func 4))
  (export "stackAlloc" (func 5))
  (elem (;0;) (i32.const 1) func 0))
