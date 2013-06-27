require 'rake'
require 'rspec/core/rake_task'

MVN_CP_CMD = "mvn dependency:build-classpath | grep -v '[INFO]'"

task default: [:spec]

RSpec::Core::RakeTask.new(:spec) do |t|
  t.pattern = "src/test/spec/**/*_spec.rb"
  t.rspec_opts = []
  t.rspec_opts << '--tty'
  t.rspec_opts << '--color'
  t.rspec_opts << '--format documentation'
  t.rspec_opts = t.rspec_opts.join(" ")
  t.ruby_opts = ""
  t.ruby_opts << '-J-Xmx1G'
  mvn_cp = `#{MVN_CP_CMD}`.strip
  t.ruby_opts << " -Isrc/test/spec"
  t.ruby_opts << " -J-classpath ./target/classes:#{mvn_cp}"

end
